package source;

import java.io.*;
import java.net.*;

public class TCPServerRouter {
    private static Object[][] routingTable = new Object[10][2];

    public static void main(String[] args) {
        int port = 5555;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ServerRouter is listening on port: " + port);
            int index = 0;

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client: " + clientSocket.getInetAddress().getHostAddress());

                // Create and start a thread to handle communication with the client
                new RouterThread(clientSocket).start();

                routingTable[index][0] = clientSocket.getInetAddress().getHostAddress();
                routingTable[index][1] = clientSocket;
                index++;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

class RouterThread extends Thread {
    private Socket clientSocket;
    private String serverName = "localhost"; // Actual TCPServer address
    private int serverPort = 5556; // TCPServer port

    public RouterThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try (Socket serverSocket = new Socket(serverName, serverPort);
             ObjectInputStream clientIn = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectOutputStream serverOut = new ObjectOutputStream(serverSocket.getOutputStream());
             ObjectInputStream serverIn = new ObjectInputStream(serverSocket.getInputStream())) {

            // Receive matrices from the client
            int[][] matrixA = (int[][]) clientIn.readObject();
            int[][] matrixB = (int[][]) clientIn.readObject();
            System.out.println("Router received matrices from client, forwarding to server...");

            // Forward matrices to the server
            serverOut.writeObject(matrixA);
            serverOut.writeObject(matrixB);

            // Receive result from the server
            int[][] result = (int[][]) serverIn.readObject();
            System.out.println("Router received result from server, forwarding to client...");

            // Send result back to the client
            clientOut.writeObject(result);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in RouterThread: " + e.getMessage());
        }
    }
}
