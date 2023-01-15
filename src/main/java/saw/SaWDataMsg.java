package saw;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import exceptions.IllegalMsgException;


//format: saw⟨WS⟩⟨seqNr⟩⟨WS⟩⟨msg⟩

public class SaWDataMsg extends SaWMsg {

    private int seqNr;

    public int getSeqNr() {
        return seqNr;
    }

    public void setSeqNr(int seqNr) {
        this.seqNr = seqNr;
    }


    protected void create(String data, int seqNr) throws IllegalMsgException {
        //check if the given sequence number is valid (0 or 1)
        if (seqNr == 0 || seqNr == 1) {
            this.seqNr = seqNr;
            this.data = data;
            // Create a new string with the data and sequence number
            String dataBytesString = SAW_HEADER + this.seqNr + "\s" + this.data;
            this.dataBytes = dataBytesString.getBytes();
        }
        // If the sequence number is not valid, throw an exception
        else {
            throw new IllegalMsgException();
        }
    }


    @Override
    protected Msg parse(String sentence) throws IWProtocolException {
        // Split the sentence into two parts: seqNr and data
        String[] parts = sentence.split("\\s+", 2);
        if(parts[0].equals("0") || parts[0].equals("1")) {
            // Create the object with the data and seqNr
            this.create(parts[1], Integer.parseInt(parts[0]));
        } else {
            throw new IllegalMsgException();
        }
        // Return the created object
        return this;
    }

}
