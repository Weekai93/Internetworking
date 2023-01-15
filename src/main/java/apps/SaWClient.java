package apps;

import exceptions.IWProtocolException;
import phy.PhyProtocol;
import saw.SaWConfiguration;
import saw.SaWDataMsg;
import saw.SaWProtocol;
import slp.SLPProtocol;

import java.io.IOException;
import java.net.InetAddress;


public class SaWClient {
    private static final String SWITCHNAME = "localhost";
    public static final int SAWCLIENTPORT = 12345;

    public static void main(String[] args) throws IWProtocolException, IOException {

        //create a new SaW configuration with the server port
        SaWConfiguration sawConfig = new SaWConfiguration(SaWServer.SAWSERVERPORT, null);

        //create a new SLP protocol
        SLPProtocol slp = new SLPProtocol(SAWCLIENTPORT, false, new PhyProtocol(SAWCLIENTPORT));

        //register the SLP protocol with the switch
        slp.register(InetAddress.getByName(SWITCHNAME), SLPSwitch.SWITCHPORT);

        //create a new SaW protocol instance with the SLP protocol
        SaWProtocol saw = new SaWProtocol(slp);

        //continuously receive SaWDataMsg messages from the server
        while (true) {
            SaWDataMsg msg = (SaWDataMsg) saw.receive(sawConfig);
            String message = msg.getData();
            System.out.println("Received message: " + message);
        }
    }
}
