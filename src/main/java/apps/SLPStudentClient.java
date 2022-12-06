package apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import core.Msg;
import phy.PhyProtocol;
import slp.SLPConfiguration;
import slp.SLPProtocol;
import exceptions.*;

public class SLPStudentClient implements Runnable {
	private static final String SWITCHNAME = "localhost";
	SLPProtocol ethP;

	public SLPStudentClient(SLPProtocol eth) {
		this.ethP = eth;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Provide an address identifier (int) from range [5000:65534]");
			return;
		}
		int phyPortNumber = Integer.parseInt(args[0]);
		if(phyPortNumber < 5000 || phyPortNumber > 65534) {
			System.out.println("Invalid address identifier! Range [5000:65534]");
			return;
		}

		// Set up the virtual link protocol
		PhyProtocol phy = new PhyProtocol(phyPortNumber);

		// Create socket 
		SLPProtocol slp;
		
		// Start Protocol and register client
		try {
			slp = new SLPProtocol(phyPortNumber, false, phy);
			slp.register(InetAddress.getByName(SWITCHNAME), SLPSwitch.SWITCHPORT);
		} catch (IWProtocolException | IOException e) {
			e.printStackTrace();
			return;
		}
		
//Remove comment for exercise 3 -->
		// Start the receiver side of the app
		SLPStudentClient recv = new SLPStudentClient(slp);
		Thread recvT = new Thread(recv);
		recvT.start();

		// Read data from user to send to client
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		boolean eof = false;
		System.out.println("Input format: <dst> <message>");
		while (!eof) {
			try {
				System.out.println("Message Destination: ");
				String sentence = null;
				sentence = inFromUser.readLine();
				int dst = Integer.parseInt(sentence.trim());

				System.out.println("Message: ");
				sentence = null;
				sentence = inFromUser.readLine();

				SLPConfiguration config = new SLPConfiguration(dst, phy);
				slp.send(sentence.trim(), config);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IWProtocolException e) {
				e.printStackTrace();
			}
		}
//<-- end uncomment for exercise 3 */
		
	}


	// Receiver thread
	@Override
	public void run() {
// Remove comment for exercise 3 -->
		boolean eof = false;
		
		while (!eof) {
			try {
				Msg msg = this.ethP.receive();
				String sentence = msg.getData().trim();
					System.out.println("Received message: " + sentence);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IWProtocolException e) {
				e.printStackTrace();
			}
		}
//<-- end uncomment for exercise 3
	
	}
}

