package apps;

import exceptions.IWProtocolException;
import phy.PhyProtocol;
import saw.SaWConfiguration;
import saw.SaWProtocol;
import slp.SLPProtocol;

import java.io.IOException;
import java.net.InetAddress;


public class SaWClient {
    private static final String SWITCHNAME = "localhost";
    public static final int SAWCLIENTPORT = 33333;

    public static void main(String[] args) throws IWProtocolException, IOException {

        SaWConfiguration sawConfig = new SaWConfiguration(SaWServer.SAWSERVERPORT, null);

        SLPProtocol slp = new SLPProtocol(SAWCLIENTPORT, false, new PhyProtocol(SAWCLIENTPORT));
        slp.register(InetAddress.getByName(SWITCHNAME), SLPSwitch.SWITCHPORT);


        SaWProtocol saw = new SaWProtocol(slp);
    }
}
