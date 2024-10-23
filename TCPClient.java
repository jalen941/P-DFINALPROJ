package source;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String routerName = "localhost"; // Router address
        int routerPort = 5555; // Router port
        try (Socket socket = new Socket(routerName, routerPort);
             ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream())) {
        	int[] matrixSizes = {1024, 2048, 4096, 8192, 16384}; // Matrix sizes


            for (int i=0 ; i < matrixSizes.length; i ++) {
            	
            
            // Generate sample matrices
            int[][] matrixA = generateMatrix(matrixSizes[i]);
            int[][] matrixB = generateMatrix(matrixSizes[i]);
            System.out.println("Client generated matrices: ");
            printMatrix(matrixA);
            printMatrix(matrixB);

            // Send matrices to the router (which will forward to the server)
            objectOut.writeObject(matrixA);
            objectOut.writeObject(matrixB);

            // Receive result from the router (which received it from the server)
            int[][] result = (int[][]) objectIn.readObject();
            System.out.println("Received result matrix from the server: ");
            printMatrix(result);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        //int rowsToPrint = Math.min(2, matrix.length); // Limit to 2 or fewer rows if matrix has less than 2
        for (int i = 0; i < 2; i++) {
            for (int val : matrix[i]) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

}
