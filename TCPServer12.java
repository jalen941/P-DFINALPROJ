import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TCPServer12 {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        InetAddress addr = InetAddress.getLocalHost();
        String routerName = "172.20.10.2"; // ServerRouter host name (replace with your IP)
        int SockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try {
            socket = new Socket(routerName, SockNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        // Message variables
        String fromServer;
        String fromClient;
        String address = "172.20.10.5"; // destination IP (Client)

        // Initial connection setup
        out.println(address);
        fromClient = in.readLine();
        System.out.println("ServerRouter: " + fromClient);

        // List to store all incoming matrices
        List<int[][]> matrices = new ArrayList<>();
        boolean readyToMultiply = false;

        // Communication while loop
        while ((fromClient = in.readLine()) != null) {
            System.out.println("Client said: " + fromClient);
        
            if (fromClient.equals("Bye.")) // exit statement
                break;
        
            // Handle the "End" message separately
            if (fromClient.equals("End")) {
                System.out.println("Received 'End', performing final multiplication...");
                // Perform Strassen multiplication of all matrices in the list
                if (matrices.size() >= 2) {
                    int[][][] matrixArray = matrices.toArray(new int[matrices.size()][][]);
                    System.out.println("Starting logic to get the result");
        
                    // Use the Strassen method to multiply all matrices
                    int[][] resultMatrix = Strassen.multiply(matrixArray, 5);
        
                    // Log result and send to client
                    String resultString = matrixToString(resultMatrix);
                    System.out.println("Multiplication result:\n" + resultString);
                    out.println(resultString);
                } else {
                    out.println("Insufficient matrices to perform multiplication.");
                }
        
                // Clear matrices after final multiplication
                matrices.clear();
                readyToMultiply = false;
            } else if (!isIPAddress(fromClient) && !fromClient.equals("*")) {
                int[][] currentMatrix = parseMessageTo2DArray(fromClient);
                System.out.println("Parsed current matrix: " + matrixToString(currentMatrix));
                matrices.add(currentMatrix); // Add matrix to list
            } else if (fromClient.equals("*")) {
                System.out.println("Received '*', ready to multiply with the next matrix.");
                readyToMultiply = true;
            }
        
            fromServer = fromClient;
            System.out.println("Server said: " + fromServer);
            out.println(fromServer); // Echo back to client
        }
        

        // Closing connections
        out.close();
        in.close();
        socket.close();
    }

    private static boolean isIPAddress(String message) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; // basic IPv4 pattern
        return Pattern.matches(ipPattern, message);
    }

    // Method to parse a string message into a 2D array
    private static int[][] parseMessageTo2DArray(String message) {
        String[] rows = message.split(";");
        int[][] matrix = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] values = rows[i].split(",");
            matrix[i] = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                matrix[i][j] = Integer.parseInt(values[j]);
            }
        }

        return matrix;
    }

    // Method to convert a 2D array to a string
    private static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            for (int value : row) {
                sb.append(value).append(",");
            }
            sb.deleteCharAt(sb.length() - 1); // Remove trailing comma
            sb.append(";"); // Adds semicolon for separating rows
        }
        return sb.toString();
    }
}
