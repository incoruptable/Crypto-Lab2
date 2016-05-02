import java.awt.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class Aggregator {
	private ServerSocket aggregatorSocket = null;
	private Socket userSocket = null;
	private static CopyOnWriteArrayList<byte[]> cipherTexts = new CopyOnWriteArrayList<byte[]>();
	private static CopyOnWriteArrayList<Integer> cipherLengths = new CopyOnWriteArrayList<Integer>();
	private static CopyOnWriteArrayList<BigInteger> secrets = new CopyOnWriteArrayList<BigInteger>();

	private byte[] key;
	private double m;
	private double sum;
	private SecretKeySpec signingKey;
	BigInteger key0;
	BigInteger bigIntM;
	private static int connections = 0;
	
	public static void main (String args[])
	{
		new Aggregator().startServer();
	}
	
	public void startServer(){
		//This is the Aggregator
		
		final ExecutorService userProcessingPool = Executors.newFixedThreadPool(2);
		
		Socket trustedSocket;
		try {
			trustedSocket = new Socket("localhost", 9090);
		
			PrintWriter out = new PrintWriter(trustedSocket.getOutputStream(),true);
			DataInputStream in= new DataInputStream(trustedSocket.getInputStream());
	
			System.out.println("Connected to localhost in port 4921 - User");
			
			//Send the response that we are the aggregator to the trustedauthority
			out.println(new String("Aggregator"));
			
			System.out.println("Waiting for keys now - Aggregator");
			
			//Read in the key the trusted authority sends
			//Thread.sleep(500);
			//System.out.println(in.read());
			//while(in.read() != 0){
				System.out.println("Reading in the key values");
				int length = in.readInt();
				byte[] secretKey = new byte[length];
				in.read(secretKey);
				
				secrets.add(new BigInteger(secretKey));
				System.out.println("Added: " + new BigInteger(secretKey));
				
				
				int length2 = in.readInt();
				byte[] secretKey2 = new byte[length2];
				in.read(secretKey2);
				
				secrets.add(new BigInteger(secretKey2));		
				
			//}
		
		
		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

			
		generateKey();
		Runnable aggregatorTask = new Runnable() {
			@Override
			public void run() {
				try{
					aggregatorSocket = new ServerSocket(4921);
					System.out.println("Waiting for connections");

					while (true){
						userSocket = aggregatorSocket.accept();
						userProcessingPool.submit(new AggregationTask(userSocket));
						connections++;
					
						try {
							Thread.sleep(500);
							decryptAggregate();
						} catch (InterruptedException e) {
					
							e.printStackTrace();
						}
						//System.out.println("Cipher text lenght: " + cipherLengths.size());
					}
				}
				catch(IOException e) {
					System.err.println("Unable to process request");
					e.printStackTrace();
				}
			}
		};
		Thread aggregatorThread = new Thread(aggregatorTask);
		aggregatorThread.start();
	}
	
	private void generateKey() {
		// Generating K0
		int delta = 123456;
		
		key = new byte[8];
		m = 200000;
		bigIntM =  new BigDecimal(m).toBigInteger();
		
		
		System.out.println("Mac about to be initialized");
		try {
			//Mac mac = Mac.getInstance("HmacSHA1"); 
			int t = 45;
			
			key0 = BigInteger.ZERO;
			
			for(int x = 0; x < secrets.size(); x++){
				
				Mac mac = Mac.getInstance("HmacSHA256"); 
				SecretKeySpec signingKey = new SecretKeySpec(secrets.get(x).toByteArray(),"HmacSHA256");
				mac.init(signingKey);
								
				byte[] hmacResult = mac.doFinal(ByteBuffer.allocate(4).putInt(t).array());
			
			
				BigInteger HMAC = new BigInteger(hmacResult);
				HMAC = HMAC.mod(bigIntM);
				System.out.println("HMAC BigINT: " + HMAC);
				
			
				key0 = key0.add(HMAC);
				
			}
			
			//key0 = key0.mod(bigIntM);
			System.out.println("Generated key0 in agg: " + key0);
			
			
		
		}catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	protected void decryptAggregate() {
		
		if(connections==2){

		BigInteger sum = new BigInteger("0");
		System.out.print("Trying to decrypt the values.");
		
		for(int x = 0; x<2; x ++){
			
			BigInteger val = new BigInteger(cipherTexts.get(x));
			
			sum = sum.add(val);
		}
		sum = sum.subtract(key0);
		sum = sum.mod(bigIntM);
		System.out.print("This is the sum aggregator got: " + sum);
		
		}
	}

	private class AggregationTask implements Runnable{

		private final Socket userSocket;
		
		
		private AggregationTask(Socket userSocket) {
			this.userSocket = userSocket;
		}
		@Override
		public void run() {
			//Reading in cipher text
			
			System.out.print("Received a connection!");
			DataInputStream dIn;
			try {
				// retrieving data from client
				dIn = new DataInputStream(userSocket.getInputStream());
				
				int cipherLength = dIn.readInt();
				byte[] cipherText = new byte[cipherLength];
				dIn.read(cipherText);
				
				
				System.out.println("cipherText received = " + new BigInteger(cipherText));
			
				cipherTexts.add(cipherText);
				cipherLengths.add(cipherLength);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
