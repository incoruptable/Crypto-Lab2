import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class Aggregator {
	private ServerSocket aggregatorSocket = null;
	private Socket userSocket = null;
	private CopyOnWriteArrayList<byte[]> cipherTexts;
	private CopyOnWriteArrayList<Integer> cipherLengths;
	private ArrayList<byte[]> authorityKeys;
	private ArrayList<Integer> keyLengths;
	private BigInteger key;
	private BigInteger m;
	private BigInteger sum;
	private ArrayList<SecretKeySpec> signingKeys;
	byte[] secretKey;
	
	public static void main (String args[])
	{
		new Aggregator().startServer();
	}
	
	public void startServer(){
		//This is the Aggregator
		
		
		keyLengths = new ArrayList<Integer>();
		authorityKeys = new ArrayList<byte[]>();
		cipherLengths = new CopyOnWriteArrayList<Integer>();
		cipherTexts = new CopyOnWriteArrayList<byte[]>();
		signingKeys = new ArrayList<SecretKeySpec>();
		
		Socket trustedSocket;
		try {
			trustedSocket = new Socket("localhost", 9090);
			
		
		DataInputStream in= new DataInputStream(trustedSocket.getInputStream());

		System.out.println("Connected to localhost in port 4921 - User");

		//Read in the key the trusted authority sends
		while(authorityKeys.size() < 2) {
			keyLengths.add(in.readInt());
			secretKey = new byte[keyLengths.get(keyLengths.size()-1)];
			
			System.out.print("Key Length = " + keyLengths.get(keyLengths.size()-1));
			
			in.read(secretKey);

			System.out.print(" Key = " + secretKey + "\n");
			authorityKeys.add(secretKey);
			//Change it to the actual secret key
			signingKeys.add(new SecretKeySpec(secretKey,"HmacSHA1"));
		}


		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
		generateKey();
		try{
			ExecutorService userProcessingPool = Executors.newFixedThreadPool(2);
			aggregatorSocket = new ServerSocket(4921);
			System.out.println("Waiting for connections");
			int threadNumber = 0;
			while (true){
				userSocket = aggregatorSocket.accept();
				threadNumber++;
				userProcessingPool.submit(new AggregationTask(userSocket));
				if(threadNumber == 2){
					break;
				}
			}
			System.out.print("cipherLengths.size() = " + cipherLengths.size());
			userProcessingPool.shutdown();
			userProcessingPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			if(cipherLengths.size() == 2){
				System.out.print("cipherLengths.size() = " + cipherLengths.size());
				decryptAggregate();
			}
		}
		catch(IOException | InterruptedException e) {
			System.err.println("Unable to process request");
			e.printStackTrace();
		}
	}
			
	
	private void generateKey() {
		// Generating K0
		int delta = 123456;
		double doublem = Math.pow(2,(Math.log(2*delta)/Math.log(2)));
		m = new BigInteger(ByteBuffer.allocate(8).putDouble(doublem).array());
		System.out.println("Mac about to be initialized");
		byte[] hMacResult;
		BigInteger HMAC = null;
		BigInteger HMACSum = new BigInteger("0");
		try {
			for(int i = 0; i < 2; i++){
					
				Mac mac = Mac.getInstance("HmacSHA1"); 
				mac.init(signingKeys.get(i));
				int t = 45;
				hMacResult = mac.doFinal(ByteBuffer.allocate(t). array());
				HMAC = new BigInteger(hMacResult);
				HMACSum = HMACSum.add(HMAC);
			}
			
			System.out.print("HMAC Sum = " + HMACSum);
			
			
			//performing MOD M
			key = HMACSum.mod(m);
			
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
		BigInteger cipherSum = new BigInteger("0");
		for(byte[] x: cipherTexts){
			BigInteger cipherText = new BigInteger(x);
			
			cipherSum = cipherSum.add(cipherText);
		}
		
		cipherSum.subtract(key);
		sum = cipherSum.mod(m);
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
			try{
				userSocket.close();
			}catch(IOException ioe){
				System.out.print("Error closing user Connection");
			}
			
		}
		
	}
}
