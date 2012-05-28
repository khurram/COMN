import java.io.*;
import java.net.*;

public class Receiver3 {

    public static void main(String args[]) throws Exception {
        System.out.println("Ready to receive the file!");

        // Get the address, port and name of file to send over UDP
        final int port = Integer.parseInt(args[0]);
        final String fileName = args[1];

        receiveAndCreate(port, fileName);
    }

    public static void receiveAndCreate(int port, String fileName) throws IOException {
        // Create the socket, set the address and create the file to be sent
        DatagramSocket socket = new DatagramSocket(port);
        InetAddress address;
        File file = new File(fileName);
        FileOutputStream outToFile = new FileOutputStream(file);

        // Create a flag to indicate the last message
        boolean lastMessageFlag = false;
		
        // Store sequence number
        int sequenceNumber = 0;
        int lastSequenceNumber = 0;

        // For each message we will receive
        while (!lastMessageFlag) {
            // Create byte array for full message and another for file data without header
            byte[] message = new byte[1024];
            byte[] fileByteArray = new byte[1021];

            // Receive packet and retreive message
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.setSoTimeout(0);
            socket.receive(receivedPacket);
            message = receivedPacket.getData();

            // Get port and address for sending ack
            address = receivedPacket.getAddress();
            port = receivedPacket.getPort();

            // Retrieve sequence number
            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);

            // Retrieve the last message flag
            if ((message[2] & 0xff) == 1) {
                lastMessageFlag = true;
            } else {
                lastMessageFlag = false;
            }

            if (sequenceNumber == (lastSequenceNumber + 1)) {

                // Update latest sequence number
                lastSequenceNumber = sequenceNumber;

                // Retrieve data from message
                for (int i=3; i < 1024 ; i++) {
                    fileByteArray[i-3] = message[i];
                }

                // Write the message to the file
                outToFile.write(fileByteArray);
                System.out.println("Received: Sequence number = " + sequenceNumber +", Flag = " + lastMessageFlag);

                // Send acknowledgement
                sendAck(lastSequenceNumber, socket, address, port);

                // Check for last message
                if (lastMessageFlag) {
                    outToFile.close();
                } 
            } else {
                // If packet has been received, send ack for that packet again
                if (sequenceNumber < (lastSequenceNumber + 1)) {
                    // Send acknowledgement for received packet
                    sendAck(sequenceNumber, socket, address, port);
                } else {
                    // Resend acknowledgement for last packet received
                    sendAck(lastSequenceNumber, socket, address, port);
                }
            }
        }
        
        socket.close();
        System.out.println("File " + fileName + " has been received.");
	}

    public static void sendAck(int lastSequenceNumber, DatagramSocket socket, InetAddress address, int port) throws IOException {
        // Resend acknowledgement
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte)(lastSequenceNumber >> 8);
        ackPacket[1] = (byte)(lastSequenceNumber);
        DatagramPacket acknowledgement = new  DatagramPacket(ackPacket, ackPacket.length, address, port);
        socket.send(acknowledgement);
        System.out.println("Sent ack: Sequence Number = " + lastSequenceNumber);
    }
}
