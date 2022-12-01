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
class SLPSwitchRegistrationHandlingTask4Test {

	int senderId = 9999;
	int senderPort = 5550;
	int receiverId = 9997;
	int receiverPort = 5551;	
	int switchPort = 4999; 
	
	SLPProtocol slpProtocol;
	PhyConfiguration phyConfig;
	PhyMsg regMsgGood;
	
	@Mock
	PhyProtocol phyProtocolMock;

	@Captor
	private ArgumentCaptor<String> captor;
	
	@BeforeEach
	void setup() throws IWProtocolException, IOException {
		
		// Set up the object-under-test
		slpProtocol = new SLPProtocol(switchPort, true, phyProtocolMock);
		
		// Create additionally needed objects
		phyConfig = new PhyConfiguration(InetAddress.getByName("localhost"), senderPort);
		regMsgGood = new PhyMsg(phyConfig);
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsgGood = (PhyMsg)regMsgGood.parse("phy slp reg " + senderId);
	}
	
	@Test
	void testRegisterSuccess() throws SocketTimeoutException, IOException, IWProtocolException {
				
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(regMsgGood);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(switchPort, true, phyProtocolMock);
		
		// Run the test
		slpProtocol.receive();
		
		// verify a specified behavior
		verify(phyProtocolMock, times(1)).receive();
		verify(phyProtocolMock, times(1)).send(captor.capture(), any(PhyConfiguration.class));
		List<String> values = captor.getAllValues();
		assertEquals("slp reg ACK", values.get(0));
	}


	@Test
	void testRegisterOnSameId() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(regMsgGood);
		
		// Run the test
		slpProtocol.receive();
		slpProtocol.receive();
		
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(2)).send(captor.capture(), any(PhyConfiguration.class));
		List<String> values = captor.getAllValues();	
		assertEquals("slp reg ACK", values.get(0));
		assertTrue(values.get(1).startsWith("slp reg NAK"));
	}

	@Test
	void testMalformedHeader() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg regMsgCorrupted = new PhyMsg(phyConfig);

		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsgCorrupted = (PhyMsg)regMsgCorrupted.parse("phy slp rg 9999");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(regMsgCorrupted).thenReturn(regMsgGood);
		
		// Run the test
		slpProtocol.receive();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(1)).send(any(String.class), any(PhyConfiguration.class));
	}
	
	@Test
	void testMalformedAddress() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg regMsgCorrupted = new PhyMsg(phyConfig);

		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsgCorrupted = (PhyMsg)regMsgCorrupted.parse("phy slp reg " + senderId + "malformed");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(regMsgCorrupted).thenReturn(regMsgGood);
		
		// Run the test
		slpProtocol.receive();
				
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(1)).send(any(String.class), any(PhyConfiguration.class));
	}
	
	@Test
	void testAddressOutOfRange() throws SocketTimeoutException, IOException, IWProtocolException {
		
		PhyMsg regMsgCorrupted = new PhyMsg(phyConfig);

		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		regMsgCorrupted = (PhyMsg)regMsgCorrupted.parse("phy slp reg 1");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive()).thenReturn(regMsgCorrupted).thenReturn(regMsgGood);
		
		// Run the test
		slpProtocol.receive(); 
				
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive();
		verify(phyProtocolMock, times(1)).send(any(String.class), any(PhyConfiguration.class));
	}

}
