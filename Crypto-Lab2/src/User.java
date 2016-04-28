import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class User {
	  Socket aggregatorSocket;
	  static final int PORTagg = 4921;
	  final int portTrusted = 9090;
	  Socket trustedSocket;
	  
	  int delta = 123456;
	  DataInputStream in;
	  byte[] secretKey;
	  double m;
	  String hmac = "HmacSHA1";
	  int t = 45;
	  int value;
	  
	  public static void main(String args[]) {
	        User user = new User();
	 }
	
	  public User(){
		  
		  Random rand = new Random(delta);
		  value = rand.nextInt(delta);
		  System.out.println("User generated value: " + value);
		  try {
				
				trustedSocket = new Socket("localhost",portTrusted);
				
				in= new DataInputStream(trustedSocket.getInputStream());
				
				System.out.println("Connected to localhost in port 4921 - User");
				
				//Read in the key the trusted authority sends
				int secret = in.readInt();
			
				//Change the integer to a byte array and put it as a secret key
				SecretKeySpec signingKey = new SecretKeySpec(ByteBuffer.allocate(4).putInt(secret).array(),hmac);
				
				m = Math.pow(2,(Math.log(2 * delta)/Math.log(2)));
				
				Mac mac = Mac.getInstance(hmac);
				mac.init(signingKey);
				
				byte[] hmacResult = mac.doFinal(ByteBuffer.allocate(4).putInt(t).array());
				
				System.out.println("hmac result: " + hmacResult);
				
				//Change hmacresult to double so we can mod?
				//BigInteger HMAC = new BigInteger(hmacResult);
				double HMAC = ByteBuffer.wrap(hmacResult).getDouble();
				
				//computer k0
				double userKey = HMAC % m;
				
				System.out.println("User key: " + userKey);

				double cipherText = (userKey + value) % m;
				
				aggregatorSocket = new Socket("localhost",PORTagg);
				
				DataOutputStream out = new DataOutputStream(aggregatorSocket.getOutputStream());
				out.writeDouble(cipherText);
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
	  }
	  

}
