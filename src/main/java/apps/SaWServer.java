package apps;

import exceptions.IWProtocolException;
import phy.PhyProtocol;
import saw.SaWConfiguration;
import saw.SaWProtocol;
import slp.SLPProtocol;

import java.io.IOException;
import java.net.InetAddress;


public class SaWServer {
    private static final String SWITCHNAME = "localhost";
    protected static final int SAWSERVERPORT = 44444;

    public static void main(String[] args) throws IWProtocolException, IOException {

        SaWConfiguration sawConfig = new SaWConfiguration(SaWClient.SAWCLIENTPORT, null);

        SLPProtocol slp = new SLPProtocol(SAWSERVERPORT, false, new PhyProtocol(SAWSERVERPORT));
        slp.register(InetAddress.getByName(SWITCHNAME), SLPSwitch.SWITCHPORT);

        SaWProtocol saw = new SaWProtocol(slp);
    }
}
