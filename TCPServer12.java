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

        // Initial connection setup
        String address = "172.20.10.5"; // destination IP (Client)
        out.println(address);
        System.out.println("ServerRouter: " + in.readLine());

        // Read matrices row-by-row from client
        List<long[][]> matrices = new ArrayList<>();
        List<long[]> currentMatrixRows = new ArrayList<>();
        String fromClient;

        while ((fromClient = in.readLine()) != null) {
            if (fromClient.equals("#")) {
                // End of one matrix
                long[][] matrix = currentMatrixRows.toArray(new long[0][0]);
                matrices.add(matrix);
                currentMatrixRows.clear();
            } else if (fromClient.equals("DONE")) {
                break;
            } else {
                // Parse the row into longs and add to the current matrix
                if (!isIPAddress(fromClient)) {
                    System.out.println("Added row: " + fromClient);
                    long[] row = parseRowToLong(fromClient);
                    currentMatrixRows.add(row);
                }
            }
        }

        // Convert list of matrices to a 3D array
        long[][][] matrixArray = matrices.toArray(new long[matrices.size()][][]);
        System.out.println("Received matrix array from client.");
        matrices = null;  // Dereference list of matrices
        currentMatrixRows = null;  // Dereference current matrix rows list

// Suggest garbage collection
        System.gc();

        // Perform Strassen multiplication (adjusted for long data type)
        long[][] resultMatrix = {};
        try {
            resultMatrix = Strassen.multiply(matrixArray, 31);  // Update Strassen to work with long
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the result to a string and send it back to the client
        String resultString = matrixToString(resultMatrix);
        System.out.println("Multiplication result:\n" + resultString);
        out.println(resultString);

        // Closing connections
        out.close();
        in.close();
        socket.close();
    }

    // Parses a comma-separated row into a long array
    private static long[] parseRowToLong(String row) {
        String[] values = row.split(",");
        long[] longRow = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                longRow[i] = Long.parseLong(values[i]);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing value: " + values[i]);
                longRow[i] = 0; // Default to 0 if parsing fails
            }
        }
        return longRow;
    }

    // Method to convert a 2D long array to a string
    private static String matrixToString(long[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (long[] row : matrix) {
            for (long value : row) {
                sb.append(value).append(",");
            }
            sb.deleteCharAt(sb.length() - 1); // Remove trailing comma
            sb.append(";"); // Adds semicolon for separating rows
        }
        return sb.toString();
    }

    private static boolean isIPAddress(String message) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; // basic IPv4 pattern
        return Pattern.matches(ipPattern, message);
    }
}
