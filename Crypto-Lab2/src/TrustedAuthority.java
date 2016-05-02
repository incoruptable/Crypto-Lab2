import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class TrustedAuthority {

    final int PORT = 9090;
    ServerSocket serverSocket;
    Socket socket;
    Socket aggSocket;
    int orderNumber;
    DataOutputStream out;
    int secretTotal = 0;
    ArrayList<BigInteger> keys = new ArrayList<BigInteger>();	
    BigInteger valueTotal = BigInteger.ZERO;
    
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
                
                System.out.println("Connection received from " + socket.getInetAddress().getHostName());
                
                //Store the aggregator socket so we can send to him once we generate keys
                System.out.println("Waiting for soemthing to be sent.");
                
                String str = new String(in.readLine());
           
                
                System.out.println("Read str: " + str);
                if(str.equals("Aggregator")){
                	System.out.println("Aggregator connected");
                	aggSocket = socket;	
                }
              
                if(secretTotal ==2){
                	
                	System.out.println("Trying to send keys to aggregator now.");
                	 out = new DataOutputStream(aggSocket.getOutputStream());
                	
                	//Sends both keys to the aggregator
                	for(int x = 0; x < keys.size(); x++){
                		System.out.println("Sending to aggregator: " + keys.get(x));
                		out.writeInt(keys.get(x).toByteArray().length);
    					out.write(keys.get(x).toByteArray());
                	}
                	System.out.println("Done sendin both keys to aggregator");
                	
                	System.out.println("The secret total the aggregator should decrypt is : " + valueTotal);
                	secretTotal = 0;
                }
                else{
                	
					Random rand = new Random();
					BigInteger secretKey = new BigInteger(56,rand);
					
					keys.add(secretKey);
					secretTotal++;
					
					System.out.println("Generated Secret key for user: " + secretKey);
					out.writeInt(secretKey.toByteArray().length);
					out.write(secretKey.toByteArray());
					System.out.println("Finished Sending keys to the user");

					
                }

            } catch (IOException e) {
                System.out.println("I/O error: " + e);
                e.printStackTrace();
            }
         
        }
    }
    
}
