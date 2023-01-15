package saw;


import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;

//format: saw⟨WS⟩ack<WS><ackNr>

public class SaWAckMsg extends SaWMsg {
    private int ackNr;

    public int getAckNr() {
        return ackNr;
    }

    public void setAckNr(int ackNr) {
        this.ackNr = ackNr;
    }

    public static final String SAW_ACK_HEADER = "ack ";


    protected void create(int ackNr) throws IllegalMsgException {

        //check if the given ack number is valid (0 or 1)
        if (ackNr == 0 || ackNr == 1) {
            this.ackNr = ackNr;
            super.create(SAW_ACK_HEADER + ackNr);
        }
        //if the acknowledgement number is not valid, throw an exception
        else {
            throw new IllegalMsgException();
        }
    }

    protected Msg parse(String sentence) throws IWProtocolException {

        String[] parts = sentence.split("\\s+", 2);
        //check if the sentence starts with SAW_HEADER
        if(parts[0].equals(SAW_ACK_HEADER)) {
            //create the object with the acknowledgement number
            this.create(Integer.parseInt(parts[1]));
            return this;
        }
        //if the sentence does not start with the SaW Header, throw an exception
        else {
            throw new IllegalMsgException();
        }

    }

}
