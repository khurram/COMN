/* Khurram Aslam 0792198 */
import java.io.*;
import java.net.*;

public class Sender1 {

	public static void main(String args[]) throws Exception {				
		// Get the address, port and name of file to send over UDP
		final String hostName = args[0];
		final int port = Integer.parseInt(args[1]);
		final String fileName = args[2];															
		
		createAndSend(hostName, port, fileName);
	}
	
	public static void createAndSend(String hostName, int port, String fileName) throws IOException {
		System.out.println("Sending the file");
		
		// Create the socket, set the address and create the file to be sent
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(hostName);
		File file = new File(fileName);	
		
		// Create a byte array to store the filestream
		InputStream inFromFile = new FileInputStream(file);
        byte[] fileByteArray = new byte[(int)file.length()];
        inFromFile.read(fileByteArray);
		
		// Create a flag to indicate the last message and a 16-bit sequence number
		int sequenceNumber = 0;
		boolean lastMessageFlag = false;
		
		// For each message we will create
		for (int i=0; i < fileByteArray.length; i = i+1021 ) {
			
			// Increment sequence number
			sequenceNumber += 1;
			
			// Create new message. Set first and second bytes of the message to sequence number
			byte[] message = new byte[1024];
			message[0] = (byte)(sequenceNumber >> 8);
			message[1] = (byte)(sequenceNumber);
				
			// Set flag to 1 if packet is last packet and store it in third byte of header
			if ((i+1021) >= fileByteArray.length) {
				lastMessageFlag = true;
				message[2] = (byte)(1);
			} else { // If not last packet, store flag as 0
				lastMessageFlag = false;
				message[2] = (byte)(0);
			}

			// Copy the bytes for the message to the message array
			if (lastMessageFlag == false) {
				for (int j=0; j <= 1020; j++) {
					message[j+3] = fileByteArray[i+j];
				}
			}
			else if (lastMessageFlag == true) { // If it is the last message
				for (int j=0;  j < (fileByteArray.length - i)  ;j++) {
					message[j+3] = fileByteArray[i+j];			
				}
			}
			
			// Send the message
			DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
			socket.send(sendPacket);
			System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);
			
			// Sleep for 20 milliseconds to avoid sending too quickly
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
		socket.close();
		System.out.println("File " + fileName + " has been sent");
	}	
}