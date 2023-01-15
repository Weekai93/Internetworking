package apps;

import exceptions.IWProtocolException;
import phy.PhyProtocol;
import saw.SaWConfiguration;
import saw.SaWProtocol;
import slp.SLPProtocol;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;


public class SaWServer {
    private static final String SWITCHNAME = "localhost";
    protected static final int SAWSERVERPORT = 54321;

    public static void main(String[] args) throws IWProtocolException, IOException {

        //create a new SaW configuration with the client port
        SaWConfiguration sawConfig = new SaWConfiguration(SaWClient.SAWCLIENTPORT, null);

        //create a new SLP protocol
        SLPProtocol slp = new SLPProtocol(SAWSERVERPORT, false, new PhyProtocol(SAWSERVERPORT));

        //register the SLP protocol with the switch
        slp.register(InetAddress.getByName(SWITCHNAME), SLPSwitch.SWITCHPORT);

        //create a new SaW protocol instance with the SLP protocol
        SaWProtocol saw = new SaWProtocol(slp);

        //create a scanner to read input from the user
        Scanner scanner = new Scanner(System.in);

        //continuously read input from the user and send it to the client using the SaW protocol
        while(true) {
            try {
                System.out.print("Enter your message: ");
                String input = scanner.nextLine();
                saw.send(input, sawConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
