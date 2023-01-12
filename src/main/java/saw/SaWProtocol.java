package saw;

import core.Configuration;
import core.Msg;
import core.Protocol;
import exceptions.IWProtocolException;
import phy.PhyProtocol;
import slp.SLPConfiguration;
import slp.SLPProtocol;

import java.io.IOException;

public class SaWProtocol extends Protocol{

    private SLPProtocol slp;
    private SLPConfiguration slpConfig;
    private int currentSeqNo;
    private int currentAckNo;


    public SaWProtocol(SLPProtocol slp) throws IWProtocolException {
        this.slp = slp;
        this.currentAckNo = 0;
        this.currentSeqNo = 0;
    }
    //sendet Daten an den Server. nur für den Client. receive nur für den Server
    @Override
    public void send(String s, Configuration config) throws IOException, IWProtocolException {
        // create a msg object
        Msg message = null;
        // send the message
        slp.send(s,slpConfig);

    }

    @Override
    public Msg receive() throws IOException, IWProtocolException {
        Msg in = null;
        in = slp.receive();
        in = (new SaWMsg()).parse(in.getData());
        return in;
    }
}
