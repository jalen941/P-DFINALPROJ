import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
//updated class needed for dynamic matrix intake
public class TCPServer12 {
    public static void main(String[] args) throws IOException {
        // Variables for setting up connection and communication
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        InetAddress addr = InetAddress.getLocalHost();
        String routerName = "172.20.10.3"; // ServerRouter host name
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
        String fromClient;
        String address = "172.20.10.7"; // destination IP (Client)

        // Initial connection setup
        out.println(address);
        fromClient = in.readLine();
        System.out.println("ServerRouter: " + fromClient);

        // Store matrices
        ArrayList<int[][]> matrixList = new ArrayList<>();
        boolean multiplicationTriggered = false;  // Track if a `*` was received

        // Communication while loop
        while ((fromClient = in.readLine()) != null) {
            System.out.println("Client said: " + fromClient);

            if (fromClient.equals("Bye.")) // exit statement
                break;

            if (!isIPAddress(fromClient) && !fromClient.equals("*")) {
                int[][] matrix = parseMessageTo2DArray(fromClient);
                System.out.println("Parsed matrix:\n" + matrixToString(matrix));

                matrixList.add(matrix);
                System.out.println("Matrix added to list. Current list size: " + matrixList.size());

            } else if (fromClient.equals("*")) {
                System.out.println("Received '*'. Ready to perform multiplication.");

                if (matrixList.size() >= 2) {
                    int[][] resultMatrix = multiplyAllMatrices(matrixList);
                    String resultString = matrixToString(resultMatrix);
                    System.out.println("Multiplication result:\n" + resultString);
                    out.println(resultString);

                    matrixList.clear(); // Clear list for next sequence
                } else {
                    System.out.println("Not enough matrices to multiply.");
                    out.println("Not enough matrices to multiply.");
                }
                multiplicationTriggered = true;
            }
        }

        // Final multiplication if no trailing `*` is received
        if (!multiplicationTriggered && matrixList.size() >= 2) {
            System.out.println("Connection closed without final '*'. Performing final multiplication.");
            int[][] resultMatrix = multiplyAllMatrices(matrixList);
            String resultString = matrixToString(resultMatrix);
            System.out.println("Final multiplication result:\n" + resultString);
            out.println(resultString);
        } else {
            System.out.println("No final multiplication needed.");
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

    // Multiply all matrices in the list sequentially
    private static int[][] multiplyAllMatrices(ArrayList<int[][]> matrices) {
        int[][] resultMatrix = matrices.get(0);
        for (int i = 1; i < matrices.size(); i++) {
            resultMatrix = Strassen.multiply(new int[][][]{resultMatrix, matrices.get(i)}, 5);
            System.out.println("Result after multiplying with matrix " + (i+1) + ":\n" + matrixToString(resultMatrix));
        }
        return resultMatrix;
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
        }
        return sb.toString();
    }
}




///////these are the updates needed for strassenn
// Method to build a tree-like structure for arbitrary matrix multiplication
private static TreeNode buildTree(int[][][] matrices) {
    TreeNode[] nodes = new TreeNode[matrices.length];
    for (int i = 0; i < matrices.length; i++) {
        nodes[i] = new TreeNode(matrices[i]);
    }

    // Build a tree-like structure from the bottom up, combining pairs of matrices
    while (nodes.length > 1) {
        int newSize = (nodes.length + 1) / 2;
        TreeNode[] nextLevelNodes = new TreeNode[newSize];

        for (int i = 0; i < newSize; i++) {
            TreeNode left = nodes[i * 2];
            TreeNode right = (i * 2 + 1 < nodes.length) ? nodes[i * 2 + 1] : null;
            nextLevelNodes[i] = new TreeNode(left, right);
        }
        nodes = nextLevelNodes;
    }
    return nodes[0];
}

// Recursive function to execute matrix multiplication in tree structure
private static int[][] executeTree(TreeNode node, int numCores) {
    if (node.isLeaf()) {
        return node.matrix;
    }

    ExecutorService executor = Executors.newFixedThreadPool(numCores);
    Future<int[][]> leftFuture = executor.submit(() -> executeTree(node.left, numCores));
    Future<int[][]> rightFuture = node.right != null ? executor.submit(() -> executeTree(node.right, numCores)) : null;

    int[][] leftResult = null;
    int[][] rightResult = null;
    try {
        leftResult = leftFuture.get(); // Left result
        if (rightFuture != null) {
            rightResult = rightFuture.get(); // Right result if it exists
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        executor.shutdown();
    }

    if (rightResult == null) {
        return leftResult; // Return the left result if there's no right node
    }

    // Multiply left and right results using Strassen
    return strassen(leftResult, rightResult, leftResult.length);
}
