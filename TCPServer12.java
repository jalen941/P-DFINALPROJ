import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TCPServer12 {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading from ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String routerName = "172.20.10.3"; // ServerRouter host name (replace with your IP)
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
        String address = "172.20.10.7"; // destination IP (Client)

        // Initial connection setup
        out.println(address);
        fromClient = in.readLine();
        System.out.println("ServerRouter: " + fromClient);

        // Store matrices
        int[][] previousMatrix = null;
        boolean readyToMultiply = false;

        // Communication while loop
        while ((fromClient = in.readLine()) != null) {
            System.out.println("Client said: " + fromClient);

            if (fromClient.equals("Bye.")) // exit statement
                break;

            if (!isIPAddress(fromClient) && !fromClient.equals("*")) {
                int[][] currentMatrix = parseMessageTo2DArray(fromClient);
                System.out.println("Parsed current matrix: " + matrixToString(currentMatrix));

                if (readyToMultiply && previousMatrix != null) {
                    // Multiply previousMatrix and currentMatrix
                    System.out.println("Multiplying matrices:");
                    System.out.println("Previous matrix:\n" + matrixToString(previousMatrix));
                    System.out.println("Current matrix:\n" + matrixToString(currentMatrix));

                    int[][] resultMatrix = Strassen.multiply(new int[][][]{previousMatrix, currentMatrix}, 5);

                    // Log result and send to client
                    String resultString = matrixToString(resultMatrix);
                    System.out.println("Multiplication result:\n" + resultString);
                    out.println(resultString);

                    // Update previousMatrix to the result for next potential multiplication
                    previousMatrix = resultMatrix;
                    readyToMultiply = false;
                } else {
                    // Store the matrix as previousMatrix for the next round
                    previousMatrix = currentMatrix;
                    System.out.println("Stored as previous matrix:\n" + matrixToString(previousMatrix));
                }
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

    // Method to convert a 2D array to a string (for uniqueness check)
    private static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            for (int value : row) {
                sb.append(value).append(",");
            }
            sb.deleteCharAt(sb.length() - 1); // Remove trailing comma
          sb.append(";"); // Adds newline for better debug formatting
               // sb.append(";"); // Adds newline for better debug formatting

        }
        //sb.deleteCharAt(sb.length() - 2); // Remove trailing newline and semicolon
        return sb.toString();
    }
}
