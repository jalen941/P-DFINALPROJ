import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TCPServer12 {
    public static void main(String[] args) throws IOException {
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

        int[] coresForMatrices = {1, 3, 7, 15, 31}; 
        int currentCoreIndex = 0;

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
                if (currentCoreIndex == coresForMatrices.length) break; 
            } else if (!isIPAddress(fromClient) && !fromClient.equals("DONE")) {
                currentMatrixRows.add(parseRowToLong(fromClient));
            }
        }

        // Close connections
        out.close();
        in.close();
        socket.close();
    }

private static void processMatrices(long[][][] matrixArray, int numCores, PrintWriter out) {
    long serialTime;

    long serialStart = System.nanoTime();
    try {
        System.out.println("starting single core execution...");
        Strassen.multiply(matrixArray, 1); 
    } catch (Exception e) {
        System.out.println("Error during single-core execution: " + e);
        return;
    }
    long serialEnd = System.nanoTime();
    serialTime = (serialEnd - serialStart) / 1_000_000; 

    long parallelStart = System.nanoTime();
    try {
        System.out.println("moving on to multi core execution...");

        Strassen.multiply(matrixArray, numCores); 
    } catch (Exception e) {
        System.out.println("Error during multi-core execution: " + e);
        return;
    }
    System.out.println("calculating metrics...");

    long parallelEnd = System.nanoTime();
    long parallelTime = (parallelEnd - parallelStart) / 1_000_000; 

    double speedup = (double) serialTime / parallelTime;
    double efficiency = speedup / numCores;
    if(numCores==1){
        speedup=1;
        efficiency=1;
        parallelTime=serialTime;
    }
    System.out.println("sending metrics to client");

    out.printf("Cores: %d | Serial Time: %d ms | Parallel Time: %d ms | Speedup: %.2f | Efficiency: %.2f%n",
            numCores, serialTime, parallelTime, speedup, efficiency);
}


    private static long[] parseRowToLong(String row) {
        String[] values = row.split(",");
        long[] longRow = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                longRow[i] = Long.parseLong(values[i]);
            } catch (NumberFormatException e) {
                longRow[i] = 0; 
            }
        }
        return longRow;
    }

    private static boolean isIPAddress(String message) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; 
        return Pattern.matches(ipPattern, message);
    }
}
