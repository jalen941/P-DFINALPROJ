package source;

import java.io.*;
import java.net.*;

public class TCPServer1 {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5556); // TCPServer port
        System.out.println("TCPServer is listening on port: 5556");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected to Router");
            System.out.println("starting mm");
            // Handle matrix multiplication in a thread
            new SThread(clientSocket).start();
        }
    }
}
