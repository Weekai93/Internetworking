package apps;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClientRunnable implements Runnable {
	private DatagramSocket clientSocket;
	
	public UDPClientRunnable() throws SocketException {		
		this.clientSocket = new DatagramSocket();
	}
	
	@Override
	public void run() {
		// Read from keyboard
		java.io.BufferedReader inFromUser =
				new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		String portString = "";
		String sentence = "";
		int port = 0;
		while (true) {
			try {
				System.out.println("Recipient: ");
				portString = inFromUser.readLine();
				port = Integer.parseInt(portString.trim());
				System.out.println("Message: ");
				sentence = inFromUser.readLine();

				// Convert String to byte array for sending
				byte[] sendData = new byte[1024];
				sendData = sentence.getBytes();
				System.out.println("Send to " + port + ": " + sentence);

				// Create datagram object
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
						InetAddress.getByName("localhost"), port);
				clientSocket.send(sendPacket);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

}
