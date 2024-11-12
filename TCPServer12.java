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
        String routerName = "172.20.10.2"; // ServerRouter host name
        int sockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try {
            socket = new Socket(routerName, sockNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Initial connection setup
        String address = "172.20.10.5"; // destination IP (Client)
        out.println(address);
        System.out.println("ServerRouter: " + in.readLine());

        // Core configurations for each matrix count
        int[] coresForMatrices = {1, 3, 7, 15, 31}; // Cores to use for 2, 4, 8, 15, 32 matrices
        int currentCoreIndex = 0;

        // Loop to process each set of matrices
        String fromClient;
        List<long[][]> matrices = new ArrayList<>();
        List<long[]> currentMatrixRows = new ArrayList<>();
        while ((fromClient = in.readLine()) != null) {
            if (fromClient.equals("#")) {
                matrices.add(currentMatrixRows.toArray(new long[0][0]));
                currentMatrixRows.clear();
            } else if (fromClient.equals("END")) {
                int numCores = coresForMatrices[currentCoreIndex++];
                processMatrices(matrices.toArray(new long[0][][]), numCores, out);
                
                out.println("Completed processing " + matrices.toArray(new long[0][][]).length + " matrices.");
                matrices.clear();
                if (currentCoreIndex == coresForMatrices.length) break; // All sets processed
            } else if (!isIPAddress(fromClient) && !fromClient.equals("DONE")) {
                currentMatrixRows.add(parseRowToLong(fromClient));
            }
        }

        // Close connections
        out.close();
        in.close();
        socket.close();
    }

// Process matrices and log performance metrics
private static void processMatrices(long[][][] matrixArray, int numCores, PrintWriter out) {
    long serialTime;

    // Measure the time it would take with 1 core for this specific instance
    long serialStart = System.nanoTime();
    try {
        System.out.println("starting single core execution...");
        Strassen.multiply(matrixArray, 1); // Run on a single core to get serial time
    } catch (Exception e) {
        System.out.println("Error during single-core execution: " + e);
        return;
    }
    long serialEnd = System.nanoTime();
    serialTime = (serialEnd - serialStart) / 1_000_000; // Serial time in ms

    // Measure the parallel execution time with the specified number of cores
    long parallelStart = System.nanoTime();
    try {
        System.out.println("moving on to multi core execution...");

        Strassen.multiply(matrixArray, numCores); // Run with the specified number of cores
    } catch (Exception e) {
        System.out.println("Error during multi-core execution: " + e);
        return;
    }
    System.out.println("calculating metrics...");

    long parallelEnd = System.nanoTime();
    long parallelTime = (parallelEnd - parallelStart) / 1_000_000; // Parallel time in ms

    // Calculate speedup and efficiency
    double speedup = (double) serialTime / parallelTime;
    double efficiency = speedup / numCores;
    if(numCores==1){
        speedup=1;
        efficiency=1;
        parallelTime=serialTime;
    }
    System.out.println("sending metrics to client");

    // Log the performance metrics
    out.printf("Cores: %d | Serial Time: %d ms | Parallel Time: %d ms | Speedup: %.2f | Efficiency: %.2f%n",
            numCores, serialTime, parallelTime, speedup, efficiency);
}


    // Helper methods
    private static long[] parseRowToLong(String row) {
        String[] values = row.split(",");
        long[] longRow = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                longRow[i] = Long.parseLong(values[i]);
            } catch (NumberFormatException e) {
                longRow[i] = 0; // Default to 0 if parsing fails
            }
        }
        return longRow;
    }

    private static boolean isIPAddress(String message) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; // basic IPv4 pattern
        return Pattern.matches(ipPattern, message);
    }
}
