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
        int dimensions = 8;
        int[] matrixCounts = {2, 4, 8, 16, 32}; 

        String address = "172.20.10.2"; // destination IP (Server)
        out.println(address);
        System.out.println("ServerRouter: " + in.readLine());
        out.println(host); // Send client IP

        for (int i=0; i < matrixCounts.length; i++ ) {
           // System.out.println(i + "th run");
            System.out.println("Sending " + matrixCounts[i] + " matrices with dimensions: " + dimensions + "x" + dimensions);
            streamMatrices(out, matrixCounts[i], dimensions);

            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Completed processing " + matrixCounts[i] + " matrices.")) {
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
            out.println("#"); 
        }
        out.println("END");
    }
}
