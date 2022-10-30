package apps;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MonolithicEchoPhyServer {
	public static void main(String[] args) {
		DatagramSocket datagramSocket;
		try {
			// Open socket
			datagramSocket = new DatagramSocket(12000);
			DatagramPacket receivePacket;
			while (true) {
				// Receive packet 
				byte[] receiveData = new byte[2048];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				datagramSocket.receive(receivePacket);
				
				String message = new String(receivePacket.getData());
				
				// Strip protocol header 
				String sentence = message.substring("phy ".length()).trim();
				System.out.println("Received message: " + sentence);
				
				// Process string
				sentence = sentence.toUpperCase();
				
				// Add protocol header
				sentence = "phy " + sentence;
				byte[] buffer = sentence.getBytes();

				// Send packet
				DatagramPacket packet = new DatagramPacket(
				        buffer, buffer.length, receivePacket.getAddress(), receivePacket.getPort());
				datagramSocket.send(packet);

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
