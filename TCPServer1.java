/*package source;

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
        String routerName = "localhost"; // ServerRouter host name
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
*/


package source;

import java.io.*;
import java.net.*;

public class TCPServer1 {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading from ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Server machine's IP
        String routerName = "localhost"; // ServerRouter host name
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

        // Send the destination client address
        String address = "localhost"; // Change this to the appropriate client address if needed
        out.println(address); // Initial send (IP of the destination Client)
        String fromClient = in.readLine(); // Initial receive from router (verification of connection)
        System.out.println("ServerRouter: " + fromClient);

        try {
            // Communication while loop
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Client said: " + fromClient);
                if (fromClient.equals("Bye.")) // exit statement
                    break;

                // Parse the matrices from the incoming string
                String[] matrixData = fromClient.split(";");
                int[][] matrixA = parseMatrix(matrixData[0]);
                int[][] matrixB = parseMatrix(matrixData[1]);

                // Perform matrix multiplication
                int[][][] matrices = {matrixA, matrixB}; // Prepare matrices for multiplication
                
                int[][] result = Strassen.multiply(matrices, 5);

                // Convert the result matrix to a string and send back
                out.println(matrixToString(result));
                System.out.println("Server sent the result matrix back to the client.");
            }
        } catch (IOException e) {
            System.err.println("Error during communication: " + e.getMessage());
        } finally {
            // Closing connections
            out.close();
            in.close();
            socket.close();
        }
    }

    // Helper method to parse matrix from string
    private static int[][] parseMatrix(String matrixStr) {
        String[] rows = matrixStr.split("\\|");
        int[][] matrix = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            matrix[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                matrix[i][j] = Integer.parseInt(cols[j]);
            }
        }
        return matrix;
    }

    // Helper method to convert a matrix to a string
    private static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            for (int val : row) {
                sb.append(val).append(",");
            }
            sb.setLength(sb.length() - 1); // Remove last comma
            sb.append("|"); // Row delimiter
        }
        sb.setLength(sb.length() - 1); // Remove last pipe
        return sb.toString();
    }
}
