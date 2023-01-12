package phy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import exceptions.IWProtocolException;
import exceptions.RegistrationFailedException;
import slp.SLPProtocol;

@ExtendWith(MockitoExtension.class)
class SLPClientRegisterTask5Test {
	
	int clientId = 9999;
	int clientPort = 5550;	
	String clientName = "localhost";
	int switchPort = 4999;
	String switchName = "localhost";
	
	@Mock
	PhyProtocol phyProtocolMock;

	@Test
	void testSendThreeMessagesWhenNoAnswersArrive() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenThrow(new SocketTimeoutException("Timeout"));
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(9999, false, phyProtocolMock);
		
		// Run the test
		assertThrows(RegistrationFailedException.class, 
				()->slpProtocol.register(InetAddress.getByName("localhost"), 9998));
		
		// verify a specified behavior
		verify(phyProtocolMock, times(3)).receive(2000);
		verify(phyProtocolMock, times(3)).send(eq("slp reg 9999"), any(PhyConfiguration.class)); 
	}
	
	@Test
	void testRegisterOnSecondTry() throws SocketTimeoutException, IOException, IWProtocolException {
		
		// Create additionally needed objects
		PhyConfiguration phyConfig = new PhyConfiguration(InetAddress.getByName("localhost"), 0);
		PhyMsg testMsg = new PhyMsg(phyConfig);
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		testMsg = (PhyMsg)testMsg.parse("phy slp reg ACK");

		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt()))
		.thenThrow(new SocketTimeoutException("Timeout"))
		.thenReturn(testMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(9999, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName("localhost"), 9998));
		
		// verify a specified behavior
		verify(phyProtocolMock, times(2)).receive(2000);
		verify(phyProtocolMock, times(2)).send(eq("slp reg 9999"), any(PhyConfiguration.class)); 
	}
}
