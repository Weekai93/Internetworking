package saw;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;
import slp.SLPMsg;

public class SaWMsg extends Msg {

    protected static final String SAW_HEADER = "saw ";

    @Override
    protected void create(String sentence) {
        this.data = sentence;
        data = SAW_HEADER + sentence;
        this.dataBytes = sentence.getBytes();

    }

    @Override
    protected Msg parse(String sentence) throws IWProtocolException {
        SaWMsg pdu = null;
        //this.dataBytes = sentence.getBytes();
        String[] parts = sentence.split("\\s+", 2);

        if (!sentence.startsWith(SAW_HEADER)) {
            System.out.println("Illegal data header: " + sentence);
            throw new IllegalMsgException();

        }

        //if second part starts with "ack" it must be a SaQAckMsg
        if (parts[1].startsWith(SaWAckMsg.SAWACKHEADER)) {
            SaWAckMsg ackMsg = new SaWAckMsg();
            return ackMsg.parse(parts[1]);
        } else {
            SaWDataMsg dataMsg = new SaWDataMsg();
            return dataMsg.parse(parts[1]);
        }
    }
}
