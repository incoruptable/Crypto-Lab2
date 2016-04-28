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
    int orderNumber;
    DataOutputStream out;
    int secretTotal = 0;
    ArrayList<BigInteger> keys = new ArrayList<BigInteger>();	
    
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

                if(secretTotal ==2){
                	
                	//Sends both keys to the aggregator
                	for(int x = 0; x < keys.size(); x++){
                		out.writeInt(keys.get(x).toByteArray().length);
    					out.write(keys.get(x).toByteArray());
                	}
            	
                }
                else{
					Random rand = new Random(293318734);
					BigInteger secretKey = new BigInteger(128,rand);
					
					keys.add(secretKey);
					secretTotal++;
					
					out.writeInt(secretKey.toByteArray().length);
					out.write(secretKey.toByteArray());
                }

            } catch (IOException e) {
                System.out.println("I/O error: " + e);
                e.printStackTrace();
            }
         
        }
    }
    
}
