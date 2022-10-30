// This class is not distributed to students

package slp;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;

/* 
 * Simple link protocol registration response message fields:
 *  -> slp registration success indicator
 *  -> [error message]
*/

public class SLPRegResponseMsg extends SLPRegMsg {
	protected static final String SLP_REG_SUCCESS = "ACK";
	protected static final String SLP_REG_FAILED = "NAK";

	private boolean regResponse;
	String regResponseMsg;

	protected boolean getRegResponse() {
		return this.regResponse;
	}
	protected void setRegResponse(boolean b) {
		this.regResponse = b;
	}

	@Override
	protected void create(String sentence) {
	}

	@Override
	protected Msg parse(String sentence) throws IWProtocolException {
		// Subtask 2.2: parse responds and set attribute accordingly
		SLPRegResponseMsg pdu = null;
		this.dataBytes = sentence.getBytes();

		if(sentence.startsWith(SLP_REG_SUCCESS)) {
			this.data = sentence;
			return this;
		}
		if(sentence.startsWith(SLP_REG_FAILED)) {
			System.out.println("Illegal data header: " + sentence);
			throw new IllegalMsgException();
		}
		throw new IllegalMsgException();


	}
}
