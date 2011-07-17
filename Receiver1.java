/* Khurram Aslam 0792198 */
import java.io.*;
import java.net.*;

public class Receiver1 {

	public static void main(String args[]) throws Exception {
		System.out.println("Ready to receive the file!");
		
		// Get the port and filename to save as		
		final int port = Integer.parseInt(args[0]);
		final String fileName = args[1];
		
		receiveAndCreate(port, fileName);
	}
	
	public static void receiveAndCreate(int port, String fileName) throws IOException {
		// Create the socket and create the file to be sent
		DatagramSocket socket = new DatagramSocket(port);
		File file = new File(fileName);
		FileOutputStream outToFile = new FileOutputStream(file);
	
		// Create a flag to indicate the last message
		boolean lastMessageFlag = false;
		boolean lastMessage = false;
		
		// Store sequence number
		int sequenceNumber = 0;
		
		// For each message we will receive
		while (!lastMessage) {
			// Create byte array for full message and another for file data without header
			byte[] message = new byte[1024];
			byte[] filebyteArray = new byte[1021];
			
			// Receive packet and retreive message
			DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
			socket.receive(receivedPacket);
			message = receivedPacket.getData();
			
			// Retrieve the sequence number
			sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);	

			// Retrieve the last message flag
			if ((message[2] & 0xff) == 1) {
				lastMessageFlag = true;
			} else {
				lastMessageFlag = false;
			}
			
			// Retrieve data from message
			for (int i=3; i < 1024 ; i++) {
				filebyteArray[i-3] = message[i];				
			}
			
			// Write the message to the file and print received message
			outToFile.write(filebyteArray);
			System.out.println("Received: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);
			
			// Check for last message
			if (lastMessageFlag) {
				outToFile.close();
				socket.close();
				lastMessage = false;
				break;
			}
		}
		socket.close();
		System.out.println("File " + fileName + " has been received.");
	}
	
}
