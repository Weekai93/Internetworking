package phy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import exceptions.IWProtocolException;
import exceptions.RegistrationFailedException;
import slp.SLPConfiguration;
import slp.SLPProtocol;

@ExtendWith(MockitoExtension.class)
class SLPClientSendTask3Test {
	SLPProtocol slpProtocol;
	PhyMsg regACKMsg;
	SLPConfiguration slpConfig;
	
	int senderId = 9999;
	int senderPort = 5550;
	int receiverId = 9997;
	int receiverPort = 5551;
	int switchPort = 4999;
	
	@Mock
	PhyProtocol phyProtocolMock;
	
	@Captor
	private ArgumentCaptor<String> stringCaptor;
	
	@Captor
	private ArgumentCaptor<PhyConfiguration> configurationCaptor;

	@BeforeEach
	void setup() throws IWProtocolException, IOException {
		// Create additionally needed objects
		PhyConfiguration phyConfig = new PhyConfiguration(InetAddress.getByName("localhost"), senderPort);
		regACKMsg = new PhyMsg(phyConfig);
		slpConfig = new SLPConfiguration(receiverId, phyProtocolMock);
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regACKMsg = (PhyMsg)regACKMsg.parse("phy slp reg ACK");
		
		// Set up the object-under-test
		slpProtocol = new SLPProtocol(senderId, false, phyProtocolMock);
	}
	
	@Test
	void testSendSuccessfully() throws SocketTimeoutException, IOException, IWProtocolException {		
		String testData = "testMessage";
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regACKMsg);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		assertDoesNotThrow(()->slpProtocol.send(testData, slpConfig));
        
		// verify a specified behavior using an ArgumentCaptor
		verify(phyProtocolMock, times(2)).send(stringCaptor.capture(), configurationCaptor.capture());
		List<String> stringValues = stringCaptor.getAllValues();
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();

		// define expected message
		String msg = "slp 9997 9999 11 testMessage 401271118";
		assertEquals(msg, stringValues.get(1));
		assertEquals(switchPort, configurationValues.get(1).getRemotePort());
		assertTrue(configurationValues.get(1).getRemoteIPAddress().toString().startsWith("localhost")); 		
	}
	
	@Test
	void testSendMessageWithoutRegistering() throws SocketTimeoutException, IOException, IWProtocolException {		
		String testData = "testMessage";

		// Run the test
		assertThrows(RegistrationFailedException.class, 
				()->slpProtocol.send(testData, slpConfig));
        
		verify(phyProtocolMock, times(0)).send(any(String.class), any(PhyConfiguration.class));
	}
	
	@Test
	void testSendEmptyMessage() throws SocketTimeoutException, IOException, IWProtocolException {
		String testData = "";

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regACKMsg);
	
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		assertDoesNotThrow(()->slpProtocol.send(testData, slpConfig));
        
		// verify a specified behavior using an ArgumentCaptor
		verify(phyProtocolMock, times(2)).send(stringCaptor.capture(), configurationCaptor.capture());
		List<String> stringValues = stringCaptor.getAllValues();
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();

		// define expected message
		String msg = "slp 9997 9999 0  0";
		assertEquals(msg, stringValues.get(1));
		assertEquals(switchPort, configurationValues.get(1).getRemotePort());
		assertTrue(configurationValues.get(1).getRemoteIPAddress().toString().startsWith("localhost"));
	}
	
	@Test
	void testSendMessageWithWhitespaces() throws SocketTimeoutException, IOException, IWProtocolException {
		String testData = "Hello World";

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(regACKMsg);
	
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), switchPort));
		assertDoesNotThrow(()->slpProtocol.send(testData, slpConfig));
        
		// verify a specified behavior using an ArgumentCaptor
		verify(phyProtocolMock, times(2)).send(stringCaptor.capture(), configurationCaptor.capture());
		List<String> stringValues = stringCaptor.getAllValues();
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();

		// define expected message
		String msg = "slp 9997 9999 11 Hello World 1243066710";
		assertEquals(msg, stringValues.get(1));
		assertEquals(switchPort, configurationValues.get(1).getRemotePort());
		assertTrue(configurationValues.get(1).getRemoteIPAddress().toString().startsWith("localhost")); 		
	}
	
}
