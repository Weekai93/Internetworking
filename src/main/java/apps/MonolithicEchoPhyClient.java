package apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MonolithicEchoPhyClient {

	public static void main(String[] args) {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket datagramSocket;
		try {
			// Read String from keyboard
			datagramSocket = new DatagramSocket();
			System.out.print("Your message for the server: ");
			String sentence = null;
			sentence = inFromUser.readLine();
			
			// Add protocol header
			sentence = "phy " + sentence; 
			
			// Send packet
			byte[] buffer = sentence.getBytes();
			DatagramPacket packet = new DatagramPacket(
			        buffer, buffer.length, InetAddress.getByName("localhost"), 12000);
			datagramSocket.send(packet);
			
			// Receive a packet
			byte[] receiveData = new byte[2048];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			datagramSocket.receive(receivePacket);
			String message = new String(receivePacket.getData());
			
			// Strip protocol header 
			message = message.substring("phy ".length()).trim();
			System.out.println("Received message: " + message);

			datagramSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
