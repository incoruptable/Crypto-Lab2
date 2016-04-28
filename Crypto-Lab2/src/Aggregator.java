import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class Aggregator {
	private ServerSocket aggregatorSocket = null;
	private Socket userSocket = null;
	private ArrayList<byte[]> cipherTexts;
	private ArrayList<Integer> cipherLengths;
	private byte[] key;
	private double m;
	private double sum;
	private SecretKeySpec signingKey;
	
	public static void main (String args[])
	{
		new Aggregator().startServer();
	}
	
	public void startServer(){
		//This is the Aggregator
		
		final ExecutorService userProcessingPool = Executors.newFixedThreadPool(2);
		
		Socket trustedSocket;
		try {
			trustedSocket = new Socket("192.252.76.94", 9090);
		
		DataInputStream in= new DataInputStream(trustedSocket.getInputStream());

		System.out.println("Connected to localhost in port 4921 - User");

		//Read in the key the trusted authority sends
		int length = in.readInt();
		byte[] secretKey = new byte[length];
		in.read(secretKey);

		//Change it to the actual secret key
		signingKey = new SecretKeySpec(secretKey,"HmacSHA1");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
						if(cipherLengths.size() == 2){
							break;
						}
					}
					decryptAggregate();
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
		m = Math.pow(2,(Math.log(2*delta)/Math.log(2)));
		System.out.println("Mac about to be initialized");
		try {
			Mac mac = Mac.getInstance("HmacSHA1"); 
			mac.init(signingKey);
			int t = 45;
			
			System.out.print("Mac intialized");
			//performing HMAC for time interval t
			byte[] hMacResult = mac.doFinal(ByteBuffer.allocate(t). array());
			double HMAC = ByteBuffer.wrap(hMacResult).getDouble();
			
			System.out.print("HMAC result = " + HMAC);
			
			
			//performing MOD M
			HMAC = HMAC % m;
			ByteBuffer.wrap(key).putDouble(HMAC);
			
			System.out.print("Key = " + key);
		}catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	protected void decryptAggregate() {
		for(byte[] x: cipherTexts){
			double cipherText = ByteBuffer.wrap(x).getDouble();
			double tempKey = ByteBuffer.wrap(key).getDouble();
			
			cipherText = cipherText - tempKey;
			double partofSum = cipherText % m;
			sum += partofSum;
		}
		System.out.print("This is the sum: " + sum);
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
				
				System.out.print("cipherLength received = " + cipherLength + "cipherText received = " + cipherText + "\n");
				
				
				cipherLengths.add(cipherLength);
				cipherTexts.add(cipherText);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
