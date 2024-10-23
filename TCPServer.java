package source;


import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        try (Socket socket = new Socket("10.78.140.215", 5555);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send server's IP address to the router
            String address ="10.78.140.215"; //InetAddress.getLocalHost().getHostAddress();
            out.println(address);;
            System.out.println("Connected to ServerRouter");

            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Received from Client: " + fromClient);
                String response = fromClient.toUpperCase(); 
                System.out.println("Sending to Client: " + response);
                out.println(response); // send back the response
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
