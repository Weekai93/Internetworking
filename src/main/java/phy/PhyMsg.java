package phy;

import core.Msg;
import exceptions.IllegalMsgException;
import slp.SLPRegMsg;

/*
 * The only message class for the phy layer
 * This layer prepends a "phy" header when sending a message
 * This layer removes the "phy" header when receiving a message
 */
public class PhyMsg extends Msg {
	protected static final String PHY_DATA_HEADER = "phy ";

	protected PhyMsg(PhyConfiguration config) {
		super();
		this.config = config;
	}
	
	/*
	 * Prepend header for sending
	 */
	@Override
	protected void create(String data) {
		this.data = data;
		data = PHY_DATA_HEADER + data;
		this.dataBytes = data.getBytes();
	}
	
	/*
	 * Does the message start with the correct header
	 *  -> if not illegal message
	 * Remove header and populate data attribute
	 */
	@Override
	protected Msg parse(String sentence) throws IllegalMsgException {
		this.dataBytes = sentence.getBytes();
		if (!sentence.startsWith(PHY_DATA_HEADER)) {
			System.out.println("Illeagal data header: " + sentence);
			throw new IllegalMsgException();
		}
		String[] parts = sentence.split("\\s+", 2);
		PhyMsg pdu;
		// If the second token start with "ping", call the PhyPingMsg parser
		if(parts[1].startsWith(PhyPingMsg.PHY_PING_HEADER)) {
			pdu = new PhyPingMsg((PhyConfiguration) this.config);
			pdu.parse(parts[1]);
		} else {
			this.data = parts[1];
			pdu = this;
		}
		return pdu;
	}

}
