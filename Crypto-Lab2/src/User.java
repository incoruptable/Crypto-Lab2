import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
	  
	  int delta = 124567;
	  DataInputStream in;
	  byte[] secretKey;
	  double m;
	  String hmac = "HmacSHA256";
	  int t = 45;
	  int value;
	  
	  public static void main(String args[]) {
	        User user = new User();
	 }
	
	  public User(){
		  
		Random rand = new Random();
		String val  = Integer.toString(rand.nextInt(delta));
		
		BigInteger value = new BigInteger(val);
			
		  
		  System.out.println("User generated value: " + value);
		  try {
				
				trustedSocket = new Socket("localhost",portTrusted);
				
				in= new DataInputStream(trustedSocket.getInputStream());
				
				System.out.println("Connected to localhost in port 9090 - User");
				
				PrintWriter out = new PrintWriter(trustedSocket.getOutputStream(),true);
				out.println("user");
			
				//Read in the key the trusted authority sends
				int length = in.readInt();
				byte[] secretByte = new byte[length];
				in.read(secretByte);
				
				//BigInteger secret
				BigInteger secret = new BigInteger(secretByte);
				System.out.println("User read in secret: " + secret);
				
				//Change the integer to a byte array and put it as a secret key
				SecretKeySpec signingKey = new SecretKeySpec(secret.toByteArray(),hmac);
				
				//m = Math.pow(2,(Math.log(2 * delta)/Math.log(2)));
				m = 200000;
				BigInteger bigIntM =  new BigDecimal(m).toBigInteger();
				
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
				
				
				boolean scanning=true;
				while(scanning)
				{
				    try
				    {

				     	aggregatorSocket = new Socket("localhost",PORTagg);
						
							
						DataOutputStream outAgg = new DataOutputStream(aggregatorSocket.getOutputStream());
						outAgg.writeInt(cipherText.toByteArray().length);
						outAgg.write(cipherText.toByteArray());
				        scanning=false;
				    }
				    catch(ConnectException e)
				    {
				        System.out.println("Connect failed, waiting and trying again");
				        try
				        {
				            Thread.sleep(2000);//2 seconds
				        }
				        catch(InterruptedException ie){
				            ie.printStackTrace();
				        }
				    } 
				}
				
				
				
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
