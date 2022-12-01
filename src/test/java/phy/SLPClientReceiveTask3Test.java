package phy;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import exceptions.IWProtocolException;
import exceptions.RegistrationFailedException;
import slp.SLPConfiguration;
import slp.SLPDataMsg;
import slp.SLPMsg;
import slp.SLPProtocol;

@ExtendWith(MockitoExtension.class)
class SLPClientReceiveTask3Test {
	SLPProtocol slpProtocol;
	SLPConfiguration slpConfig;
	PhyMsg regMsg;
	PhyMsg dataMsg;
	PhyMsg corruptedMsg;
	// Define message content
	String testData = "testMessage";
	// define expected message
	String msg = "phy slp 9997 9999 11 testMessage 401271118";

	int senderId = 9999;
	int senderPort = 5550;
	int receiverId = 9997;
	int receiverPort = 5551;
	int switchPort = 4999;


	@Mock
	PhyProtocol phyProtocolMock;
		
	@BeforeEach
	void setup() throws IWProtocolException, IOException {
		// Create additionally needed objects
		PhyConfiguration phyConfig = new PhyConfiguration(InetAddress.getByName("localhost"), senderPort);
		regMsg = new PhyMsg(phyConfig);
		dataMsg = new PhyMsg(phyConfig);
		corruptedMsg = new PhyMsg(phyConfig);
		slpConfig = new SLPConfiguration(receiverId, phyProtocolMock);
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsg = (PhyMsg)regMsg.parse("phy slp reg ACK");
		dataMsg = (PhyMsg)dataMsg.parse(msg);
		
		// Set up the object-under-test
		slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
	}
	
	@Test
	void testReceivingSuccessfullyDataMsgAfterRegistration() throws SocketTimeoutException, IOException, IWProtocolException {
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
		assertEquals(senderId, a.getSrc());
	}
	
	@Test
	void testReceivingDataMsgWithoutRegistration() throws SocketTimeoutException, IOException, IWProtocolException {
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertThrows(RegistrationFailedException.class, 
				()->slpProtocol.receive());
	}
	
	// After registration ignore all other message types, discard message and receive next message
	@Test
	void testReceivingRegistrationMsgAfterRegistration() throws SocketTimeoutException, IOException, IWProtocolException {
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(regMsg).thenReturn(dataMsg);

		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPMsg a = (SLPMsg) slpProtocol.receive();	
		assertEquals(testData, a.getData());
	}

	@Test
	void testReceivingMessageWithWhitespaces() throws SocketTimeoutException, IOException, IWProtocolException {
		testData = "test Message with Whitespaces ";
		// define expected message
		msg = "phy slp 9997 9999 30 test Message with Whitespaces  3507135666";
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		dataMsg = (PhyMsg)dataMsg.parse(msg);

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
		assertEquals(senderId, a.getSrc());
	}

	@Test
	void testReceivingEmptyMessage() throws SocketTimeoutException, IOException, IWProtocolException {
		testData = "";
		// define expected message
		msg = "phy slp 9997 9999 0  0";
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		dataMsg = (PhyMsg)dataMsg.parse(msg);

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
		assertEquals(senderId, a.getSrc());
	}
	
	// If message destination is not the own SLP ID, discard message and receive next message
	@Test
	void testReceivingMalformedReceiverId() throws SocketTimeoutException, IOException, IWProtocolException {
		// define expected message
		String corruptedString = "phy slp 9998 9999 11 testMessage 401271118";
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		corruptedMsg = (PhyMsg)corruptedMsg.parse(corruptedString);

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(corruptedMsg).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
	}

	// If checksum does not fit data, discard message and receive next message
	@Test
	void testReceivingMessageWithMalformedChecksum() throws SocketTimeoutException, IOException, IWProtocolException {
		// define expected message
		String corruptedString = "phy slp 9997 9999 11 testMessage 1234";
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		corruptedMsg = (PhyMsg)corruptedMsg.parse(corruptedString);

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(corruptedMsg).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
	}
	
	
	// If message is incorrectly formatted, discard message and receive next message
	@Test
	void testReceivingMessageWithMalformedLength() throws SocketTimeoutException, IOException, IWProtocolException {
		// define expected message
		String corruptedString = "phy slp 9997 9999 12 testMessage 401271118";
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		corruptedMsg = (PhyMsg)corruptedMsg.parse(corruptedString);

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regMsg);
		when(phyProtocolMock.receive()).thenReturn(corruptedMsg).thenReturn(dataMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(receiverId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		SLPDataMsg a = (SLPDataMsg) slpProtocol.receive();
        
		assertEquals(testData, a.getData());
	}


}
