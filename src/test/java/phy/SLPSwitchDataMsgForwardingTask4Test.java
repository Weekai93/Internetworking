package phy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import slp.SLPProtocol;

@ExtendWith(MockitoExtension.class)
class SLPSwitchDataMsgForwardingTask4Test {

	int senderId = 9999;
	int senderPort = 5550;
	int receiverId = 9997;
	int receiverPort = 5551;	
	int switchPort = 4999; // muss kleiner sein als 5000!!!!
	
	SLPProtocol slpProtocol;
	PhyConfiguration phyConfigSender;
	PhyConfiguration phyConfigReceiver;
	PhyMsg regMsgSender;
	PhyMsg regMsgReceiver;
	PhyMsg dataMsg;
	String msg;
	
	@Mock
	PhyProtocol phyProtocolMock;

	@Captor
	private ArgumentCaptor<String> stringCaptor;
	
	@Captor
	private ArgumentCaptor<PhyConfiguration> configurationCaptor;
	
	@BeforeEach
	void setup() throws IWProtocolException, IOException {
		
		// Set up the object-under-test
		slpProtocol = new SLPProtocol(switchPort, true, phyProtocolMock);
		
		// Create additionally needed objects
		phyConfigSender = new PhyConfiguration(InetAddress.getByName("localhost"), senderPort);
		phyConfigReceiver = new PhyConfiguration(InetAddress.getByName("localhost"), receiverPort);

		regMsgSender = new PhyMsg(phyConfigSender);
		regMsgReceiver = new PhyMsg(phyConfigReceiver);
		dataMsg = new PhyMsg(phyConfigSender);

		// Calculate Checksum
		String testData = "testSuccess";
		msg = "slp " + receiverId + " " + senderId + " " + testData.length() + " " + testData + " 3461809795";
		dataMsg = (PhyMsg)dataMsg.parse("phy " + msg);
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsgSender = (PhyMsg)regMsgSender.parse("phy slp reg " + senderId);
		regMsgReceiver = (PhyMsg)regMsgReceiver.parse("phy slp reg " + receiverId);
	}
	
	@Test
	void testForwardMessageSuccessfully() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(regMsgReceiver)
		.thenReturn(dataMsg);
		
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward();
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(3)).receive();
		verify(phyProtocolMock, times(3)).send(stringCaptor.capture(), configurationCaptor.capture());
		
		List<String> stringValues = stringCaptor.getAllValues();
		assertEquals(msg, stringValues.get(2));
		
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();
		assertEquals(receiverPort, configurationValues.get(2).getRemotePort()); 
		assertTrue(configurationValues.get(2).getRemoteIPAddress().toString().startsWith("localhost")); 
	}
	
	@Test
	void testSenderNotRegistered() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgReceiver)
		.thenReturn(dataMsg);
		
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward();
		
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(1)).send(stringCaptor.capture(), any(PhyConfiguration.class));
		List<String> values = stringCaptor.getAllValues();
		
		assertEquals("slp reg ACK", values.get(0));
	}
	
	@Test
	void testReceiverNotRegistered() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(dataMsg);
	
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(1)).send(stringCaptor.capture(), any(PhyConfiguration.class));
		List<String> values = stringCaptor.getAllValues();
		
		assertEquals("slp reg ACK", values.get(0));
	}
	
	@Test
	void testForwardMessageWithWhitespaces() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Calculate Checksum
		String testData = "test message with Whitespaces ";
		msg = "slp " + receiverId + " " + senderId + " " + testData.length() + " " + testData + " 1629809262";
		dataMsg = (PhyMsg)dataMsg.parse("phy " + msg);
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(regMsgReceiver)
		.thenReturn(dataMsg);
		
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward();
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(3)).receive();
		verify(phyProtocolMock, times(3)).send(stringCaptor.capture(), configurationCaptor.capture());
		
		List<String> stringValues = stringCaptor.getAllValues();
		assertEquals(msg, stringValues.get(2));
		
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();
		assertEquals(receiverPort, configurationValues.get(2).getRemotePort()); 
		assertTrue(configurationValues.get(2).getRemoteIPAddress().toString().startsWith("localhost")); 
	}
	
	@Test
	void testForwardEmptyMessage() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg corruptedMsg = new PhyMsg(phyConfigSender);
		String testData = "";
		String msg = "slp " + receiverId + " " + senderId + " " + testData.length() + " " + testData + " 0";
		corruptedMsg = (PhyMsg)corruptedMsg.parse("phy " + msg);
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(regMsgReceiver)
		.thenReturn(corruptedMsg)
		.thenReturn(dataMsg);
		
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward();
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(3)).receive();
		verify(phyProtocolMock, times(3)).send(stringCaptor.capture(), configurationCaptor.capture());
		
		List<String> stringValues = stringCaptor.getAllValues();
		assertEquals(msg, stringValues.get(2));
		
		List<PhyConfiguration> configurationValues = configurationCaptor.getAllValues();
		assertEquals(receiverPort, configurationValues.get(2).getRemotePort()); 
		assertTrue(configurationValues.get(2).getRemoteIPAddress().toString().startsWith("localhost")); 
	}
	
	@Test
	void testForwardMessageWithWrongChecksum() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg corruptedMsg = new PhyMsg(phyConfigSender);
		// Calculate Checksum
		String testData = "testSuccess";
		String msg = "slp " + receiverId + " " + senderId + " " + testData.length() + " " + testData + " " + "1234";
		corruptedMsg = (PhyMsg)corruptedMsg.parse("phy " + msg);
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(regMsgReceiver)
		.thenReturn(corruptedMsg)
		.thenReturn(dataMsg);
		
		// Run the test
		slpProtocol.forward();
		slpProtocol.forward(); 
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(4)).receive();
		verify(phyProtocolMock, times(3)).send(stringCaptor.capture(), configurationCaptor.capture()); 
	}

	@Test
	void testForwardMessageWithMalformedLength() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg corruptedMsg = new PhyMsg(phyConfigSender);
		String testData = "testSuccess";
		String msg = "slp " + receiverId + " " + senderId + " " + (testData.length()-2) + " " + testData + " 3461809795";
		corruptedMsg = (PhyMsg)corruptedMsg.parse("phy " + msg);
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive())
		.thenReturn(regMsgSender)
		.thenReturn(regMsgReceiver)
		.thenReturn(corruptedMsg)
		.thenReturn(dataMsg);

		// Run the test
		slpProtocol.forward();  
		slpProtocol.forward(); 
		slpProtocol.forward();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(4)).receive();
		verify(phyProtocolMock, times(3)).send(stringCaptor.capture(), configurationCaptor.capture()); 
	}
	
}
