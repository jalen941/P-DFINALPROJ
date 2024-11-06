
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Strassen {

    // Entry point to multiply matrices transmitted from client
    public static int[][] multiply(int[][][] matrices, int numCores) {
        int n = matrices.length; // Number of matrices
        // Validate that n is a power of 2
        if (n <= 0 || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Number of matrices must be a power of 2.");
        }
        
        // Build binary tree from matrices
        TreeNode root = buildTree(matrices);
        
        // Execute the multiplication process
        return executeTree(root, numCores);
    }

    // Method to build the binary tree
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

    private static int[][] strassen(int[][] A, int[][] B, int n) {
        if (n == 1) {
            return new int[][]{{A[0][0] * B[0][0]}};
        }

        // Split matrices into quadrants
        int k = n / 2;
        int[][] A11 = new int[k][k], A12 = new int[k][k], A21 = new int[k][k], A22 = new int[k][k];
        int[][] B11 = new int[k][k], B12 = new int[k][k], B21 = new int[k][k], B22 = new int[k][k];

        splitMatrix(A, A11, A12, A21, A22, k);
        splitMatrix(B, B11, B12, B21, B22, k);

        // Compute M1 to M7 using Strassen's method
        int[][] M1 = strassen(add(A11, A22), add(B11, B22), k);
        int[][] M2 = strassen(add(A21, A22), B11, k);
        int[][] M3 = strassen(A11, subtract(B12, B22), k);
        int[][] M4 = strassen(A22, subtract(B21, B11), k);
        int[][] M5 = strassen(add(A11, A12), B22, k);
        int[][] M6 = strassen(subtract(A21, A11), add(B11, B12), k);
        int[][] M7 = strassen(subtract(A12, A22), add(B21, B22), k);

        // Combine results into a single matrix C
        return combineResults(M1, M2, M3, M4, M5, M6, M7, k);
    }

    private static void splitMatrix(int[][] original, int[][] A11, int[][] A12, int[][] A21, int[][] A22, int k) {
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                A11[i][j] = original[i][j];
                A12[i][j] = original[i][j + k];
                A21[i][j] = original[i + k][j];
                A22[i][j] = original[i + k][j + k];
            }
        }
    }

    private static int[][] combineResults(int[][] M1, int[][] M2, int[][] M3, int[][] M4, int[][] M5, int[][] M6, int[][] M7, int k) {
        int n = 2 * k;
        int[][] C = new int[n][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                C[i][j] = M1[i][j] + M4[i][j] - M5[i][j] + M7[i][j];
                C[i][j + k] = M3[i][j] + M5[i][j];
                C[i + k][j] = M2[i][j] + M4[i][j];
                C[i + k][j + k] = M1[i][j] - M2[i][j] + M3[i][j] + M6[i][j];
            }
        }
        return C;
    }

    private static int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    private static int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

}
