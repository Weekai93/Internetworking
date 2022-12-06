package slp;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;
import phy.PhyConfiguration;
import phy.PhyMsg;
import phy.PhyPingMsg;

/*
 * Base msg class of the SL protocol
 */
public class SLPMsg extends Msg {
	protected int localID;
	
	protected static final String SLP_HEADER = "slp ";
	protected static final int LOW_PORT = 5000;
	protected static final int HIGH_PORT = 65534;

	protected static boolean validateAddress(int addr) {
		if(addr < LOW_PORT || addr > HIGH_PORT)
			return false;
		return true;
	}
	
	/*
	 * Prepend slp header to message
	 */
	@Override
	protected void create(String data) {
		data = SLP_HEADER + data;
		this.dataBytes = data.getBytes();
	}
	
	/*
	 * Test whether the sentence object starts with "slp".
	 * If the next token is "reg" then call the SLPRegMsg parser.
	 * Otherwise call the SLPDataMsg parser.
	 * On error throw a suitable exception, otherwise return the message object.
	 */
	@Override
	protected Msg parse(String sentence) throws IWProtocolException {
		SLPMsg pdu = null;
		// Subtask 2.2: Test for registration response token and call RegMsg.parse()
		this.dataBytes = sentence.getBytes();
		//throw IllegalMsg exception if the parsed message does not start with "slp" --> parsing fails
		if (!sentence.startsWith(SLP_HEADER)) {
			System.out.println("Illegal data header: " + sentence);
			throw new IllegalMsgException();
		}
		String[] parts = sentence.split("\\s+", 2);
		// If the second token start with "reg", call the SLPRegMsg parser
		if(parts[1].startsWith(SLPRegMsg.SLP_REG_HEADER)) {
			pdu = new SLPRegMsg();
			pdu.parse(parts[1]);
			this.data = pdu.getData();
			return pdu;

			// Subtask 3.2: If first token is not a reg header try to parse as a SLP data message
		} else {
			pdu = new SLPDataMsg();
			pdu.parse(parts[1]);

		}
		return pdu;
	}


}
