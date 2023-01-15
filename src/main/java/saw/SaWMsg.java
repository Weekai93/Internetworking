package saw;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;
import slp.SLPMsg;

public class SaWMsg extends Msg {

    public static final String SAW_HEADER = "saw ";


    /*
     * Prepend saw header to message
     */
    @Override
    protected void create(String sentence) {
        this.data = sentence;
        sentence = SAW_HEADER + sentence;
        this.dataBytes = sentence.getBytes();

    }

    @Override
    protected Msg parse(String sentence) throws IWProtocolException {

        String[] parts = sentence.split("\\s+", 2);

        //throw IllegalMsg exception if the parsed message does not start with "saw"
        if (!sentence.startsWith(SAW_HEADER)) {
            System.out.println("Illegal data header: " + sentence);
            throw new IllegalMsgException();

        }

        //If the second token start with "ack", call the ackMsg parser
        if (parts[1].startsWith(SaWAckMsg.SAW_ACK_HEADER)) {
            SaWAckMsg ackMsg = new SaWAckMsg();
            return ackMsg.parse(parts[1]);

        //otherwise call the the dataMsg parser
        } else {
            SaWDataMsg dataMsg = new SaWDataMsg();
            return dataMsg.parse(parts[1]);
        }
    }
}
