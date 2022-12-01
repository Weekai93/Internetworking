package slp;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import java.util.zip.CRC32;
//format: slp{WS}{dest_addr}{WS}{src_addr}{WS}{data_len}{WS}{data}{WS}{crc}
public class SLPDataMsg extends SLPMsg {

    private int src;
    private int dest;
    private int msgLength;

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

    SLPDataMsg(int dest, int src) throws IWProtocolException {
        if(!validateAddress(dest) || !validateAddress(src)){
        throw new IllegalAddrException();
        }
        else {
            this.dest = dest;
            this.src = src;
        }
    }

    SLPDataMsg(){}

    public void create (String data){

        this.data = data;
        this.msgLength = data.length();
        CRC32 crc = new CRC32();
        crc.update(data.getBytes());

    }
}
