package source;

import java.io.*;
import java.net.*;

public class TCPServer1 {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null; // socket to connect with ServerRouter
        ObjectOutputStream objectOut = null; // for sending matrices to ServerRouter
        ObjectInputStream objectIn = null; // for receiving matrices from ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Server machine's IP            
        String routerName = "172.20.10.2"; // ServerRouter host name
        int sockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try {
            socket = new Socket(routerName, sockNum);
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        try {
            // Receive the destination client address (though it may not be needed now)
            String address = (String) objectIn.readObject();
            System.out.println("Server received destination client address: " + address);

            // Receive matrices from the client via ServerRouter
            int[][] matrixA = (int[][]) objectIn.readObject();
            int[][] matrixB = (int[][]) objectIn.readObject();
            System.out.println("Server received matrices:");

            int[][][] matrices = {matrixA, matrixB}; // Example matrices

            // Perform matrix multiplication
            int[][] result = Strassen.multiply(matrices, 5);

            // Send the result matrix back to the client
            objectOut.writeObject(result);
            System.out.println("Server sent the result matrix back to the client.");
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during communication: " + e.getMessage());
        } finally {
            // closing connections
            try {
                objectOut.close();
                objectIn.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing the connection: " + e.getMessage());
            }
        }
    }


}