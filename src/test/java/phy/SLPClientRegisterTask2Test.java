package phy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import exceptions.IWProtocolException;
import exceptions.RegistrationFailedException;
import slp.SLPProtocol;

@ExtendWith(MockitoExtension.class)
class SLPClientRegisterTask2Test {
	
	int clientId = 9999;
	int clientPort = 5550;	
	String clientName = "localhost";
	int switchPort = 4999;
	String switchName = "localhost";
	
	@Mock
	PhyProtocol phyProtocolMock;
	PhyMsg testMsg;

	@BeforeEach
	void setup() {
		// Create additionally needed objects for every test
		PhyConfiguration phyConfig;
		try {
			phyConfig = new PhyConfiguration(InetAddress.getByName(clientName), clientPort);
			testMsg = new PhyMsg(phyConfig);
		} catch (UnknownHostException e) {
			fail();
		}		
	}
	
	@Test
	@DisplayName("Registration successful test")
	void RegistrationSuccessfulTest() throws IOException, IWProtocolException {
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		testMsg = (PhyMsg)testMsg.parse("phy slp reg ACK");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(testMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(clientId, false, phyProtocolMock);
		
		// Run the test
		assertDoesNotThrow(()->slpProtocol.register(InetAddress.getByName(switchName), switchPort));
		
		// verify a specified behavior
		verify(phyProtocolMock, times(1)).receive(2000);
		verify(phyProtocolMock).send(eq("slp reg " + clientId), any(PhyConfiguration.class));
        
	}

	@Test
	void testRegistrationNotAcknowledged() throws IOException, IWProtocolException {

		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		testMsg = (PhyMsg)testMsg.parse("phy slp reg NAK");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(testMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(clientId, false, phyProtocolMock);
		
		// Run the test, assert that the tested method does not throw an exception (junit 5.2)
		assertThrows(RegistrationFailedException.class,
				()->slpProtocol.register(InetAddress.getByName(switchName), switchPort));
		
		// verify a specified behavior
		verify(phyProtocolMock, times(1)).receive(2000);
		verify(phyProtocolMock).send(eq("slp reg 9999"), any(PhyConfiguration.class));
        
	}

	
	@Test
	void testRegistrationFailedExceptionIsThrownWhenReceivingAMalformedMessage() throws IOException, IWProtocolException {
		
		// Fill the message object that is going to be returned to the object-under-test
		// with the message needed for this test case
		testMsg = (PhyMsg)testMsg.parse("phy sp rg ACK");
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenReturn(testMsg);
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(clientId, false, phyProtocolMock);
		
		// Run the test
		assertThrows(RegistrationFailedException.class, 
				()->slpProtocol.register(InetAddress.getByName(switchName), switchPort));
	}

	@Test
	void testMessageLoss() throws IOException, IWProtocolException {
		
		// Implement behavior of the mocked object
		when(phyProtocolMock.receive(anyInt())).thenThrow(new SocketTimeoutException());
		
		// Set up the object-under-test
		SLPProtocol slpProtocol = new SLPProtocol(clientId, false, phyProtocolMock);
		
		// Run the test
		assertThrows(RegistrationFailedException.class, 
				()->slpProtocol.register(InetAddress.getByName(switchName), switchPort));
	}


}