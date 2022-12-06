package slp;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import exceptions.IllegalMsgException;

/* 
 * Simple link protocol registration request message fields:
 * 	-> RegHeader
 *  -> slp id
*/


public class SLPRegMsg extends SLPMsg {
	protected static final String SLP_REG_HEADER = "reg ";

	private int slpid;
	
	protected int getSlpId() {
		return slpid;
	}

	/*
	* Create registration message. This message concatenates the reg header with the SLP ID.
	* The slp header is prepended in the super-class.
	*/
	@Override
	protected void create(String data) {
		// prepend reg header
		data =  SLP_REG_HEADER + data;
		// super class prepends slp header
		super.create(data);
	}
	
	/*
	 * This method should be called by SLPMsg.parse only.
	 * Tokenize the given string object.
	 * Test if the first token is 'reg'.
	 * Test if next token is ACK or NAK -> call registration response parser
	 */
	@Override
	protected Msg parse(String sentence) throws IWProtocolException {
		// Subtask 2.2: Test for registration response token and call SLPRegResponse.parse()
		SLPRegMsg pdu = null;
		this.dataBytes = sentence.getBytes();
		//throw IllegalMsg exception if the parsed message does not start with "reg" --> parsing fails
		if (!sentence.startsWith(SLP_REG_HEADER)) {
			System.out.println("Illegal data header: " + sentence);
			throw new IllegalMsgException();
		}

		String[] parts = sentence.split("\\s+", 2);

		// If the second token start with "ACK" or "NAK", call the SLPRegResponseMsg parser
		if(parts[1].startsWith(SLPRegResponseMsg.SLP_REG_SUCCESS) || parts[1].startsWith(SLPRegResponseMsg.SLP_REG_FAILED )) {
			pdu = new SLPRegResponseMsg();
			pdu.parse(parts[1]);
			this.data = pdu.getData();
			return pdu;

			// Subtask 4.2b: Parse registration messages
			//parse incoming reg messages and store the id
		} else {
			this.create(parts[1]);
			this.slpid = Integer.parseInt(parts[1]);
			pdu = this;
		}
		return this;
	}
}
