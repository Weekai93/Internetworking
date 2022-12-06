package slp;
import core.Msg;
import exceptions.BadChecksumException;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import exceptions.IllegalMsgException;
import java.util.zip.CRC32;

public class SLPDataMsg extends SLPMsg {

    CRC32 crc = new CRC32();
    private int src;
    private int dest;
    private int msgLength;
    private long checkSum;


    public long getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(long checkSum) {
        this.checkSum = checkSum;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public SLPDataMsg() {}

    public SLPDataMsg(int dest, int src) throws IWProtocolException {
        this.dest = dest;
        this.src = src;
    }

    //create data msg in the given format
    //format: slp{WS}{dest_addr}{WS}{src_addr}{WS}{data_len}{WS}{data}{WS}{crc}
    public void create(String data) {

        this.data = data;
        this.msgLength = data.length();
        crc.update(data.getBytes());
        this.checkSum = crc.getValue();

        String message = SLP_HEADER + this.dest + "\s" + this.src + "\s" + this.msgLength + "\s" + this.data + "\s" + this.checkSum;
        this.dataBytes = message.getBytes();
    }

    @Override
    protected Msg parse(String sentence) throws IWProtocolException {

        String[] parts = sentence.split("\\s+", 4);

        //assign destination-,source- and message length field to their corresponding attributes
        this.dest = Integer.parseInt(parts[0]);
        this.src = Integer.parseInt(parts[1]);
        this.msgLength = Integer.parseInt(parts[2]);



        //set data to empty string if the length of the message is 0
        if (this.msgLength == 0) {
            this.create("");

        //separate message from checksum and assign them to their corresponding attributes
        } else {
            this.data = parts[3].substring(0, parts[3].lastIndexOf(" "));
            this.checkSum = Long.parseLong(parts[3].replace(this.data, "").strip());
        }
        crc.update(data.getBytes());


        //check if msgLength from the message and actual data length match
        if (this.msgLength != this.data.length()) {
                throw new IllegalMsgException();
            }

        //check if checkSum from the message and actual checksum match
        if (this.checkSum != crc.getValue()) {
                throw new BadChecksumException();
            }

        return this;
    }


}
