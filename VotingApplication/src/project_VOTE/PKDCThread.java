package project_VOTE;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
 
public class PKDCThread implements Runnable{	
	
	private String id;
	private Socket socket;
	private PublicKey publicKey;
	HashMap<String, PublicKey> publicKeyStorage = new HashMap<String, PublicKey>();
	private final static Lock lock = new ReentrantLock();
	public PKDCThread(Socket socket,HashMap<String,PublicKey> publicKeyStorage) {
		this.socket=socket;
		this.publicKeyStorage=publicKeyStorage;
	}
	public void run() {
			try {
				//Gets the id of the connected user or Voting Server.
				DataInputStream inID = new DataInputStream(socket.getInputStream());
				id = (inID.readLine()+ '\n');
				
				//If hashmap doesn't contains key value of id, id and public key are stored into the hashmap..
				if(!publicKeyStorage.containsKey(id)) {
				//Gets the encoded public key from the user or voting server and generate the public key.
				ObjectInputStream inPublicKey=new ObjectInputStream(socket.getInputStream());
				byte[] bytePublicKey = (byte[]) inPublicKey.readObject();
				X509EncodedKeySpec spec2 = new X509EncodedKeySpec(bytePublicKey);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = kf.generatePublic(spec2);
				publicKeyStorage.put(id, publicKey);
				
				}
				//If the connected user is voting server
				if(id.equals("0"+'\n')) {
					//If a user votes, Voting server will send the user's id to the PKDC in order to get the public key of user.
					DataInputStream inID1 = new DataInputStream(socket.getInputStream());
					id = (inID1.readLine()+ '\n');
				
					ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
					VSKeyOutputStream.writeObject(publicKeyStorage.get(id).getEncoded());

					
				}
				//If the connected user is not voting server,The user gets the public key from the voting server
				else {
					ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
					VSKeyOutputStream.writeObject(publicKeyStorage.get("0"+'\n').getEncoded());
				}
				
				
			} catch (IOException e) {
				System.out.println("IO operation errors.(PKDCThread)");
			} catch (ClassNotFoundException e) {
				System.out.println("Couldn't reach the class with the specified name.(PKDCThread)");
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Specified algorithm is invalid.(PKDCThread)");
			} catch (InvalidKeySpecException e) {
				System.out.println("Key is invalid.(PKDCThread)");
			}
		 finally {	
			 		try {
						socket.close();
					} catch (IOException e) {
						System.out.println("IO operation errors.(PKDCThread)");					}
		         }
		
	}

	
}
