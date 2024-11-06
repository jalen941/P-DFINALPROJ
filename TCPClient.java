import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.*;

 public class TCPClient {
    public static void main(String[] args) throws IOException {
       
         // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading form ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Client machine's IP
        System.out.println(host);
        String routerName = "172.20.10.2"; // ServerRouter host name (Last IP used was Jalen's on my IPhone)
        int SockNum = 5555; // port number
         
         // Tries to connect to the ServerRouter
      try {
         Socket = new Socket(routerName, SockNum);
         out = new PrintWriter(Socket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
      } 
          catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
         } 
          catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
         }
             
       // ADJUST THE DIMENSIONS, NUMBER OF MATRICES, AND THE FILE THAT WILL BE USED HERE.
       // DON'T CHANGE THE METHODS.
      try {
         generateAndWriteMatrices(32, 2048, "finaltesting.txt");
      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }


        Reader reader = new FileReader("finaltesting.txt"); 
        BufferedReader fromFile =  new BufferedReader(reader); // reader for the string file
        String fromServer; // messages received from ServerRouter
        String fromUser; // messages sent to ServerRouter
        String address ="172.20.10.2"; // destination IP (Server) (Last IP used was Jalen's on my IPhone)
        long t0, t1, t;
         
        // Communication process (initial sends/receives
        out.println(address);// initial send (IP of the destination Server)
        fromServer = in.readLine();//initial receive from router (verification of connection)
        System.out.println("ServerRouter: " + fromServer);
        out.println(host); // Client sends the IP of its machine as initial send
        t0 = System.currentTimeMillis();
       
         // Communication while loop
      while ((fromServer = in.readLine()) != null) {
         System.out.println("Server: " + fromServer);
             t1 = System.currentTimeMillis();
         if (fromServer.equals("Bye.")) // exit statement
            break;
             t = t1 - t0;
             System.out.println("Cycle time: " + t);
       
         fromUser = fromFile.readLine(); // reading strings from a file
         if (fromUser != null) {
            System.out.println("Client: ");
            System.out.println("client sent out "+ fromUser);
            out.println(fromUser); // sending the strings to the Server via ServerRouter
                 t0 = System.currentTimeMillis();
         }
      }

       
         // closing connections
      out.close();
      in.close();
      Socket.close();

      
   }

   public static void generateAndWriteMatrices(int numberOfMatrices, int dimensions, String fileName) throws Exception {
      try (FileWriter writer = new FileWriter(fileName)) {
         int counter = numberOfMatrices;
         for (int i = 0; i < numberOfMatrices; i++) {
      
            int[][] temp = generateMatrix(dimensions,dimensions);
            writeMatrix(temp, writer, fileName);
            counter --;
            if(counter != 0){
               writer.write("\n*\n");
            }
         }

         writer.write("\nEnd\n");  // This writes "End" followed by a newline to the file

     } catch (IOException e) {
         System.err.println("An error occurred while writing to the file.");
         e.printStackTrace();
     }

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

public static void writeMatrix(int[][] matrix, FileWriter writer, String fileName) throws Exception {

   for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
         writer.write(matrix[i][j] + (j < matrix[i].length - 1 ? "," : ""));
      }
      if (i < matrix.length) {
         writer.write(";");  // Separate rows with ";"
      }
    }
    
}
 }
