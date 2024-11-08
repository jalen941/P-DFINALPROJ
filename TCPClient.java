import java.io.*;
import java.net.*;
import java.util.Random;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Client machine's IP
        System.out.println(host);
        String routerName = "172.20.10.2"; // ServerRouter host name
        int sockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try {
            socket = new Socket(routerName, sockNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        // Define dimensions and number of matrices to generate and send
        int numberOfMatrices = 2;
        int dimensions = 1024;

        // Send initial information and receive confirmation
        String address = "172.20.10.2"; // destination IP (Server)
        out.println(address);
        String fromServer = in.readLine(); // Initial receive from router
        System.out.println("ServerRouter: " + fromServer);
        out.println(host); // Send client IP

        long t0, t1, t;
        t0 = System.currentTimeMillis();

        // Stream the matrix data to the server
        streamMatrices(out, numberOfMatrices, dimensions);
        System.out.println("Client streamed 3D matrix data to server.");

        // Await response from server
        while ((fromServer = in.readLine()) != null) {
            System.out.println("Server: " + fromServer);
            t1 = System.currentTimeMillis();
            if (fromServer.equals("Bye.")) // Exit statement
                break;
            t = t1 - t0;
            System.out.println("Cycle time: " + t);
            t0 = System.currentTimeMillis();
        }

        // Closing connections
        out.close();
        in.close();
        socket.close();
    }

    // Streams a 3D matrix as row data to the server without storing the entire matrix
    public static void streamMatrices(PrintWriter out, int numberOfMatrices, int dimensions) {
        System.out.println("Sending matrices with dimensions: " + dimensions + "x" + dimensions);
        Random rand = new Random();
    
        // Loop through the number of matrices to send
        for (int k = 0; k < numberOfMatrices; k++) {
            System.out.println("Sending matrix " + (k + 1));
            
            // Loop through each row of the matrix
            for (int i = 0; i < dimensions; i++) {
                StringBuilder row = new StringBuilder();
                
                // Generate random numbers for each row, ensure they are 2 digits wide
                for (int j = 0; j < dimensions; j++) {
                    // Format the number to always be two digits (if needed)
                    row.append(String.format("%02d", rand.nextInt(100))).append(",");
                }
                
                // Remove trailing comma
                row.deleteCharAt(row.length() - 1);
                
                // Print row length and the actual row for debugging
                System.out.println("Row length: " + row.length());
                System.out.println("Row data: " + row.toString());
                
                // Send the row to the server
                out.println(row.toString()); 
            }
            
            // After each matrix, send a separator '#'
            out.println("#");
        }
        
        // Once all matrices are sent, indicate that the process is done
        out.println("DONE");
    }
    
    
}


