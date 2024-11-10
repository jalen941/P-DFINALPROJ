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
        String routerName = "172.20.10.2"; // ServerRouter host name
        int sockNum = 5555; // port number

        // Connect to the ServerRouter
        try {
            socket = new Socket(routerName, sockNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Define dimensions for matrices
        int dimensions = 256;
        int[] matrixCounts = {2, 4, 8, 15, 32}; // Number of matrices to send in each round

        // Send initial address information and receive confirmation
        String address = "172.20.10.2"; // destination IP (Server)
        out.println(address);
        System.out.println("ServerRouter: " + in.readLine());
        out.println(host); // Send client IP

        // Stream the matrix data in increments
        for (int count : matrixCounts) {
            System.out.println("Sending " + count + " matrices with dimensions: " + dimensions + "x" + dimensions);
            streamMatrices(out, count, dimensions);

            // Await response from server after processing
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Completed processing " + count + " matrices.")) {
                    break;
                }
            }
        }

        // Close connection after all matrices have been processed
        out.println("DONE");
        out.close();
        in.close();
        socket.close();
    }

    // Streams matrices to the server
    public static void streamMatrices(PrintWriter out, int numberOfMatrices, int dimensions) {
        Random rand = new Random();
   
        for (int k = 0; k < numberOfMatrices; k++) {
            for (int i = 0; i < dimensions; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < dimensions; j++) {
                    row.append(String.format("%02d", rand.nextInt(9))).append(",");
                }
                row.deleteCharAt(row.length() - 1);
                out.println(row.toString());
            }
            out.println("#"); // End of matrix indicator
        }
        out.println("END"); // End of this batch of matrices
    }
}
