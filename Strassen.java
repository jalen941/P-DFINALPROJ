import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class Strassen {

    public static long[][] multiply(long[][][] matrices, int numCores) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numCores);

        Node[] leafNodes = new Node[matrices.length];
        for (int i = 0; i < matrices.length; i++) {
            leafNodes[i] = new Node(matrices[i]);
        }
        while (leafNodes.length > 1) {
            Node[] parentNodes = new Node[(leafNodes.length + 1) / 2];
            for (int i = 0; i < leafNodes.length; i += 2) {
                if (i + 1 < leafNodes.length) {
                    parentNodes[i / 2] = new Node(leafNodes[i], leafNodes[i + 1], executor);
                } else {
                    System.out.println("Odd number of leaves, something is wrong");
                }
            }
            leafNodes = parentNodes; 
        }

        Node root = leafNodes[0];
        long[][] finalProduct = root.getResult();

        executor.shutdown();
        return finalProduct;
    }

    public static long[][] strassenMultiply(long[][] A, long[][] B) {
        int n = A.length;
        long[][] C = new long[n][n];

        if (n == 1) {
            C[0][0] = A[0][0] * B[0][0];
            return C;
        }

        int newSize = n / 2;
        long[][] a11 = new long[newSize][newSize];
        long[][] a12 = new long[newSize][newSize];
        long[][] a21 = new long[newSize][newSize];
        long[][] a22 = new long[newSize][newSize];

        long[][] b11 = new long[newSize][newSize];
        long[][] b12 = new long[newSize][newSize];
        long[][] b21 = new long[newSize][newSize];
        long[][] b22 = new long[newSize][newSize];

        split(A, a11, 0, 0);
        split(A, a12, 0, newSize);
        split(A, a21, newSize, 0);
        split(A, a22, newSize, newSize);

        split(B, b11, 0, 0);
        split(B, b12, 0, newSize);
        split(B, b21, newSize, 0);
        split(B, b22, newSize, newSize);

        long[][] M1 = strassenMultiply(add(a11, a22), add(b11, b22));
        long[][] M2 = strassenMultiply(add(a21, a22), b11);
        long[][] M3 = strassenMultiply(a11, subtract(b12, b22));
        long[][] M4 = strassenMultiply(a22, subtract(b21, b11));
        long[][] M5 = strassenMultiply(add(a11, a12), b22);
        long[][] M6 = strassenMultiply(subtract(a21, a11), add(b11, b12));
        long[][] M7 = strassenMultiply(subtract(a12, a22), add(b21, b22));

        long[][] c11 = add(subtract(add(M1, M4), M5), M7);
        long[][] c12 = add(M3, M5);
        long[][] c21 = add(M2, M4);
        long[][] c22 = add(subtract(add(M1, M3), M2), M6);

        join(c11, C, 0, 0);
        join(c12, C, 0, newSize);
        join(c21, C, newSize, 0);
        join(c22, C, newSize, newSize);

        return C;
    }

    private static long[][] add(long[][] A, long[][] B) {
        int n = A.length;
        long[][] C = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    private static long[][] subtract(long[][] A, long[][] B) {
        int n = A.length;
        long[][] C = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    private static void split(long[][] parent, long[][] child, int row, int col) {
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child.length; j++) {
                child[i][j] = parent[i + row][j + col];
            }
        }
    }

    private static void join(long[][] child, long[][] parent, int row, int col) {
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child.length; j++) {
                parent[i + row][j + col] = child[i][j];
            }
        }
    }
}

class Node {
    private long[][] matrix;
    private Node left;
    private Node right;
    private Future<long[][]> resultFuture;

    public Node(long[][] matrix) {
        this.matrix = matrix;
    }

    public Node(Node left, Node right, ExecutorService executor) {
        this.left = left;
        this.right = right;
        this.resultFuture = executor.submit(new Callable<long[][]>() {
            public long[][] call() throws Exception {
                long[][] leftResult = left.getResult();
                long[][] rightResult = right.getResult();
                return Strassen.strassenMultiply(leftResult, rightResult);
            }
        });
    }

    public long[][] getResult() throws Exception {
        if (resultFuture != null) {
            return resultFuture.get();
        }
        return matrix;
    }
}
