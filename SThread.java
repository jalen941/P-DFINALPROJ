package source;

import java.io.*;
import java.net.*;

public class SThread extends Thread {
    private Socket clientSocket;

    public SThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (ObjectInputStream objectIn = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream objectOut = new ObjectOutputStream(clientSocket.getOutputStream())) {

            // Receive matrices from the router
            int[][] matrixA = (int[][]) objectIn.readObject();
            int[][] matrixB = (int[][]) objectIn.readObject();
            System.out.println("Server received matrices for multiplication");

            // Perform Strassen's algorithm for multiplication
            int[][][] matrices = {matrixA, matrixB}; // Example matrices

            
            int[][] result = Strassen.multiply(matrices,5);
            System.out.println("Server computed result matrix, sending back to router");

            // Send result back to the router
            objectOut.writeObject(result);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in SThread: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Couldn't close socket: " + e.getMessage());
            }
        }
    }
}
