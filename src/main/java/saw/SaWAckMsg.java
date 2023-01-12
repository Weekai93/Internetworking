package saw;

import core.Msg;
import exceptions.IWProtocolException;

public class SaWAckMsg extends Msg {
    protected static final String SAWACKHEADER = "ack";
    private int ackNo;

    public SaWAckMsg() {
    }

    @Override
    protected void create(String sentence) {

    }

    @Override
    protected Msg parse(String sentence) throws IWProtocolException {
        return null;
    }
}
