/* Khurram Aslam 0792198 */
import java.io.*;
import java.net.*;

public class Sender2 {	
	
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
		
		// Start timer for calculating throughput
		StartTime timer = new StartTime(0);
        
		// Create a flag to indicate the last message and a 16-bit sequence number
		int sequenceNumber = 0;
		boolean lastMessageFlag = false;
		
		// 16-bit sequence number for acknowledged packets
		int ackSequenceNumber = 0;
		
		// Create a counter to count number of retransmissions
		int retransmissionCounter = 0;
		
		// For as each message we will create
		for (int i=0; i < fileByteArray.length; i = i+1021 ) {
			
			// Increment sequence number
			sequenceNumber += 1;
			
			// Create new message
			byte[] message = new byte[1024];
			
			// Set the first and second bytes of the message to the sequence number
			message[0] = (byte)(sequenceNumber >> 8);
			message[1] = (byte)(sequenceNumber);
				
			// Set flag to 1 if packet is last packet and store it in third byte of header
			if ((i+1021) >= fileByteArray.length) {
				lastMessageFlag = true;
				message[2] = (byte)(1);
			} else { // If not last message store flag as 0
				lastMessageFlag = false;
				message[2] = (byte)(0);
			}

			// Copy the bytes for the message to the message array
			if (!lastMessageFlag) {
				for (int j=0; j <= 1020; j++) {
					message[j+3] = fileByteArray[i+j];
				}
			}
			else if (lastMessageFlag) { // If it is the last message
				for (int j=0;  j < (fileByteArray.length - i)  ;j++) {
					message[j+3] = fileByteArray[i+j];			
				}
			}
			
			// Send the message
			DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
			socket.send(sendPacket);
			System.out.println("Sent: Sequence number = " + sequenceNumber + ", Flag = " + lastMessageFlag);
			
			// For verifying the acknowledgements
			boolean ackRecievedCorrect = false;
			boolean ackPacketReceived = false;
			
			while (!ackRecievedCorrect) {			
				// Check for an ack
				byte[] ack = new byte[2];
				DatagramPacket ackpack = new DatagramPacket(ack, ack.length);
				
				try {
					socket.setSoTimeout(50);
					socket.receive(ackpack);
					ackSequenceNumber = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);
					ackPacketReceived = true;
				} catch (SocketTimeoutException e) {
					System.out.println("Socket timed out waiting for an ack");
					ackPacketReceived = false;
					//e.printStackTrace();
				}
			
				// Break if there is an ack so that the next packet can be sent
				if ((ackSequenceNumber == sequenceNumber) && (ackPacketReceived)) {	
					ackRecievedCorrect = true;
					System.out.println("Ack received: Sequence Number = " + ackSequenceNumber);
					break;
				} else { // Resend packet
					socket.send(sendPacket);
					System.out.println("Resending: Sequence Number = " + sequenceNumber);
					
					// Increment retransmission counter
					retransmissionCounter += 1;
				}
			}
		}
		socket.close();
		System.out.println("File " + fileName + " has been sent");
		
		// Calculate the average throughput
		int fileSizeKB = (fileByteArray.length) / 1024;
		int transferTime = timer.getTimeElapsed() / 1000;
		double throughput = (double) fileSizeKB / transferTime;
		System.out.println("File size: " + fileSizeKB + "KB, Transfer time: " + transferTime + " seconds. Throughput: " + throughput + "KBps");
		System.out.println("Number of retransmissions: " + retransmissionCounter);	
	}
}