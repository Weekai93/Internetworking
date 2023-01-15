package slp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
	public void enableTimeout() {
		this.useTimeout = true;
	}
	public void disableTimeout() {
		this.useTimeout = false;
	}
	
	// Register an end systems 
	public void register(InetAddress rname, int rp) throws IWProtocolException, IOException {
		// Create configuration object
		this.phyConfig = new PhyConfiguration(rname, rp);

		int attempts = 0;
		while (attempts < 3) {
			// Create registration message object
			SLPRegMsg reg = new SLPRegMsg();
			// Fill registration message fields
			reg.create(Integer.toString(this.myID));

			phy.send(new String (reg.getDataBytes()), this.phyConfig);

			// Subtask 2: Receive response message, parse it, and inform app

			//create Msg object to store the received message object
			Msg response;
			//try to receive message within the timeout
			try	{
				response = phy.receive(SLPTIMEOUT);
				// If a timeout occurs, resend the registration message
			} catch (SocketTimeoutException e) {
				attempts++;
				//throw new RegistrationFailedException();
				continue;
			}
			//create SLPMsg into which the response data is parsed
			SLPMsg regMsg = new SLPMsg();
			try {
				regMsg.parse(response.getData());
			} catch (IWProtocolException e) {
				// If the received message is corrupted, resend the registration message
				attempts++;
				//throw new RegistrationFailedException();
				continue;
			}

			// If the response code equals "NAK", throw a RegistrationFailedException
			if (regMsg.getData().equals("NAK")) {
				System.out.println("Registration failed: " + regMsg.getData());
				//throw new RegistrationFailedException();
			}
			// If the response code equals "ACK", set the isRegistered flag to true and return
			if (regMsg.getData().equals("ACK")) {
				this.isRegistered = true;
				return;
			}
			// If the response code is invalid, throw an IllegalMsgException
			throw new IllegalMsgException();
		}
		// If the third registration attempt fails, throw a RegistrationFailedException
		if (attempts == 3) {
			throw new RegistrationFailedException();
		}
	}



	// Create SLPDataMsg object (subtask 3.3) and send
	// Subtask 3.1
	@Override
	public void send(String s, Configuration config) throws IOException, IWProtocolException {
		//if client is registered take the message that was created using SLPDataMsg.create and
		//send it using the phy config
		if (isRegistered) {
			int destAddress = ((SLPConfiguration)config).getRemoteID();
			SLPDataMsg slpDataMsg = new SLPDataMsg (destAddress, this.myID);
			slpDataMsg.create(s);
			phy.send(new String(slpDataMsg.getDataBytes()),this.phyConfig);
		}
		else {
			throw new RegistrationFailedException();
			}
		
	}

	// Receive message from underlying protocol, parse (subtasks 2.3, 2.4) and process
	// Subtask 3.2
	@Override
	public Msg receive() throws IOException, IWProtocolException {

		Msg in = null;
		SLPMsg slpMsg = new SLPMsg();

		//try to receive and parse the message
		try {
			//enhance the SLP protocol to use a timeout
			if (this.useTimeout) {
				in = this.phy.receive(SLPTIMEOUT);
			} else {
				in = this.phy.receive();
			}
			slpMsg = (SLPMsg)slpMsg.parse((in.getData()));
		}
		// discard message and receive again
		catch (IWProtocolException e) {
			slpMsg = null;
			return this.receive();
		}

		//check what kind of message SLPMsg.parse was returned
		if(slpMsg.getClass() == SLPRegMsg.class) {
			if (!this.isRegistered){
				return slpMsg;
			}
			//Check if we run as switch
			if(this.isSwitch) {
				this.registration();
			}
			else {
				//discard any incoming SLPRegMsg if already registered
				slpMsg = null;
				return this.receive();
			}
		}

		//Check if we successfully registered
		if (!this.isRegistered){
			throw new RegistrationFailedException();
		}

		SLPDataMsg slpDataMsg = (SLPDataMsg) slpMsg;
		//discard message if the destination is not the own SLP ID and receive next message
		if (this.myID != slpDataMsg.getDest()) {
			slpDataMsg = null;
			slpMsg = null;
			return this.receive();
		}
		// if message was parsed correctly object is returned to caller
		return slpMsg;
	}

	public void storeAndForward() throws IOException, IWProtocolException {
		while (true) {
			forward();
		}
	}

	public void forward() throws IOException, IWProtocolException {

		Msg receivedMsg = null;
		//listen for message
		receivedMsg = receive();

		if(receivedMsg.getClass() == SLPRegMsg.class) {
			this.registration();

		}
		if(receivedMsg.getClass() == SLPDataMsg.class) {
			SLPDataMsg slpDataMsg = (SLPDataMsg) receivedMsg;
			PhyConfiguration phyConfiguration = systems.get(((SLPDataMsg) receivedMsg).getDest());
			phy.send(receivedMsg.getData(), phyConfiguration);

			}



	}
	public void registration(){}
}
