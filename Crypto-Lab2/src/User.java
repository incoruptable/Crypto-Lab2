import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ConnectException;
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
		  
		Random rand = new Random(293318734);
		BigInteger value = new BigInteger(128,rand);
			
		  
		  System.out.println("User generated value: " + value);
		  try {
				
				trustedSocket = new Socket("localhost",portTrusted);
				
				in= new DataInputStream(trustedSocket.getInputStream());
				
				System.out.println("Connected to localhost in port 4921 - User");
				
				//Read in the key the trusted authority sends
				int length = in.readInt();
				byte[] secretByte = new byte[length];
				in.read(secretByte);
				
				//BigInteger secret
				BigInteger secret = new BigInteger(secretByte);
			
				//Change the integer to a byte array and put it as a secret key
				SecretKeySpec signingKey = new SecretKeySpec(secret.toByteArray(),hmac);
				
				m = Math.pow(2,(Math.log(2 * delta)/Math.log(2)));
				
				BigInteger bigIntM = new BigInteger(ByteBuffer.allocate(8).putDouble(m).array());
				
				Mac mac = Mac.getInstance(hmac);
				mac.init(signingKey);
				
				byte[] hmacResult = mac.doFinal(ByteBuffer.allocate(4).putInt(t).array());
				
				System.out.println("hmac result: " + hmacResult);
				
				//Change hmacresult to double so we can mod?
				BigInteger HMAC = new BigInteger(hmacResult);
		
				//computer k0
				BigInteger userKey = HMAC.mod(bigIntM);
				
				System.out.println("User key: " + userKey);

				BigInteger cipherText = userKey.add(value).mod(bigIntM);
				
				while(true){
					try{
						aggregatorSocket = new Socket("localhost",PORTagg);
						break;
					}
					catch(ConnectException e)
					{
						System.out.print("Connection failed, waiting to try again \n");
						try{
							Thread.sleep(2000);
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
				DataOutputStream out = new DataOutputStream(aggregatorSocket.getOutputStream());
				out.writeInt(cipherText.toByteArray().length);
				out.write(cipherText.toByteArray());
				
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
