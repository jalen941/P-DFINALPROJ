import java.io.*;
import java.net.*;
import java.util.Random;



public class test {

    public static void main(String[] args){
        int[][] matrix = generateMatrix(4, 4);
        int[][] matrix1 = generateMatrix(4, 4);
        writeTwoMatricesToFile(matrix,matrix1, "file.txt");
        int[][][] matrices = decodeMatricesFromFile("file.txt");
        
        // Print the matrices
        System.out.println("Decoded Matrix 1:");
        printMatrix(matrices[0]);
        
        System.out.println("Decoded Matrix 2:");
        printMatrix(matrices[1]);

        System.out.println(encodeMatrix(matrices[0]));

    }
    

         // Method to generate a matrix of random integers
 public static int[][] generateMatrix(int rows, int cols) {
    Random rand = new Random();
    int[][] matrix = new int[rows][cols];
 
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            matrix[i][j] = rand.nextInt(100); // generate random integers between 0-99
        }
    }
    return matrix;
 }
 
 // Method to write the matrix to a text file
 public static void writeTwoMatricesToFile(int[][] matrix1, int[][] matrix2, String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
        writeSingleMatrix(matrix1, writer);  // Write first matrix
        writer.write("\n*\n");               // Separator between matrices
        writeSingleMatrix(matrix2, writer);  // Write second matrix
        System.out.println("Matrices written to " + fileName);
    } catch (IOException e) {
        System.err.println("An error occurred while writing to the file.");
        e.printStackTrace();
    }
}

// Helper method to write a single matrix to a writer, with rows separated by ";"
private static void writeSingleMatrix(int[][] matrix, FileWriter writer) throws IOException {
    for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix[i].length; j++) {
            writer.write(matrix[i][j] + (j < matrix[i].length - 1 ? "," : ""));
        }
        if (i < matrix.length - 1) {
            writer.write(";");  // Separate rows with ";"
        }
    }
}
 
public static int[][][] decodeMatricesFromFile(String fileName) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        StringBuilder content = new StringBuilder();
        String line;
        
        // Read entire file content
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        
        // Split the content by "*"
        String[] matrixStrings = content.toString().split("\\*");
        int[][][] matrices = new int[2][][];  // Array to hold two 2D matrices
        
        // Decode each matrix
        for (int m = 0; m < matrixStrings.length; m++) {
            String[] rows = matrixStrings[m].split(";");  // Split rows by ";"
            int numRows = rows.length;
            int numCols = rows[0].split(",").length;
            
            // Initialize the matrix
            int[][] matrix = new int[numRows][numCols];
            
            // Populate the matrix
            for (int i = 0; i < numRows; i++) {
                String[] cells = rows[i].split(",");  // Split cells by ","
                for (int j = 0; j < numCols; j++) {
                    matrix[i][j] = Integer.parseInt(cells[j]);
                }
            }
            matrices[m] = matrix;
        }
        return matrices;
    } catch (IOException e) {
        System.err.println("An error occurred while reading the file.");
        e.printStackTrace();
        return null;
    }
}

// Utility method to print a matrix
public static void printMatrix(int[][] matrix) {
    for (int[] row : matrix) {
        for (int cell : row) {
            System.out.print(cell + " ");
        }
        System.out.println();
    }
}


public static String encodeMatrix(int[][] matrix) {
    StringBuilder encodedMatrix = new StringBuilder();
    for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix[i].length; j++) {
            encodedMatrix.append(matrix[i][j]);
            if (j < matrix[i].length - 1) {
                encodedMatrix.append(",");  // Separate columns by ","
            }
        }
        if (i < matrix.length - 1) {
            encodedMatrix.append(";");  // Separate rows by ";"
        }
    }
    return encodedMatrix.toString();
}

 



}
