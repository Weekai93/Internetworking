package slp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import core.Configuration;
import core.Msg;
import core.Protocol;
import phy.PhyConfiguration;
import phy.PhyProtocol;
import exceptions.*;


public class SLPProtocol extends Protocol {
	private static final int SLPTIMEOUT = 2000;
	private int myID;
	private final PhyProtocol phy;
	private final boolean isSwitch;
	private boolean isRegistered;
	private boolean useTimeout;
	// Switches map slp id to virtual link
	private Map<Integer, PhyConfiguration> systems;
	PhyConfiguration phyConfig;

	// Constructor
	public SLPProtocol(int id, boolean isSwitch, PhyProtocol proto) throws IWProtocolException {
		if(isSwitch == true && id < 5000) {
			systems = new HashMap<>();
		} else {
			if(SLPRegMsg.validateAddress(id)  == false )
				throw new IllegalAddrException();
			this.myID = id;
			this.isRegistered = false;
		}
		this.isSwitch = isSwitch;
		this.phy = proto;
	}
	
	/*
	 * Enable/disable the use of timeout when reading from socket
	 */
	private void enableTimeout() {
		this.useTimeout = true;
	}
	private void disableTimeout() {
		this.useTimeout = false;
	}
	
	// Register an end systems 
	public void register(InetAddress rname, int rp) throws IWProtocolException, IOException {		
		// Create registration message object 
		SLPRegMsg reg = new SLPRegMsg();
		// Fill registration message fields
		reg.create(Integer.toString(this.myID));
		// Create configuration object
		this.phyConfig = new PhyConfiguration(rname, rp);

		phy.send(new String (reg.getDataBytes()), this.phyConfig);
		
		// Subtask 2: Receive response message, parse it, and inform app

		//create Msg object to store the received message object
		Msg response;
		//try to receive message within the timeout
		try	{
			response = phy.receive(SLPTIMEOUT);
			//throw RegistrationFailed exception if no response is received within the timeout
		} catch (SocketTimeoutException e) {
			throw new RegistrationFailedException();
		}
		//create SLPMsg into which the response data is parsed
		SLPMsg regMsg = new SLPMsg();
		try {
			regMsg.parse(response.getData());
		} catch (IWProtocolException e) {
			throw new RegistrationFailedException();
		}

		//throw RegistrationFailedException if response code equals "NAK"
		if (regMsg.getData().equals("NAK")) {
			System.out.println("Registration failed");
			throw new RegistrationFailedException();
		}
		//set isRegistered flag to true and return if response code equals "ACK"
		if (regMsg.getData().equals("ACK")) {
			this.isRegistered = true;
			return;

		}
		throw new IllegalMsgException();

	}
	
	// Create SLPDataMsg object (subtask 3.3) and send
	// Subtask 3.1
	@Override
	public void send(String s, Configuration config) throws IOException, IWProtocolException {
		
	}

	// Receive message from underlying protocol, parse (subtasks 2.3, 2.4) and process
	// Subtask 3.2
	@Override
	public Msg receive() throws IOException, IWProtocolException {
		SLPMsg in = null;

		return in;
	}

	public void storeAndForward() throws IOException {
		while (true) {
			forward();
		}
	}

	public void forward() throws IOException {
	}
}
