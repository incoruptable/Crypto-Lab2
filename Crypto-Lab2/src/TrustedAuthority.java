import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class TrustedAuthority {

    final int PORT = 9090;
    ServerSocket serverSocket;
    Socket socket;
    int orderNumber;
    DataOutputStream out;
    int secretTotal = 0;
    		
    public static void main(String args[]) {
    	TrustedAuthority server = new TrustedAuthority ();
    }
    
  
    public TrustedAuthority() {
        serverSocket = null;
        socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for connection. - Trusted Authority");
        } catch (IOException e) {
            e.printStackTrace();

        }
             
        BufferedReader in;
        
        while (true) {
            try {
            	//Accept the connection
                socket = serverSocket.accept();   
                
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                
                System.out.println("Trusted Authority recieved connection from " + serverSocket.getInetAddress().getHostName());

                //Trusted authority should just connect to all users and distribute the keys immediately
                
                KeyGenerator keyGen;
                SecretKey key;
			
					//keyGen = KeyGenerator.getInstance("HmacMD5");
					//key = keyGen.generateKey();
					
		            // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
		           /* keyGen = KeyGenerator.getInstance("HmacSHA1");
		            key = keyGen.generateKey();
		            
		            //Change to byte array
		            byte[] keyEncoded = key.getEncoded();
		            */
					
					Random rand = new Random(293318734);
					int secret = rand.nextInt(34985039);
					
					secretTotal+=secret;
					
					out.writeInt(secret);
					
					
		            //Write the lenght of the byte array and then the key itself to whoever has connected
		            //out.writeInt(keyEncoded.length);
		           // out.write(keyEncoded);
		            
		
				
           

                     
    
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
                e.printStackTrace();
            }
         
        }
    }
    
}
