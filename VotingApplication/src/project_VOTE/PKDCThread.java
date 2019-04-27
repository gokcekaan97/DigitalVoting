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
				
				DataInputStream inID = new DataInputStream(socket.getInputStream());
				id = (inID.readLine()+ '\n');
				
				if(!publicKeyStorage.containsKey(id)) {
				ObjectInputStream inPublicKey=new ObjectInputStream(socket.getInputStream());
				byte[] bytePublicKey = (byte[]) inPublicKey.readObject();
				X509EncodedKeySpec spec2 = new X509EncodedKeySpec(bytePublicKey);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = kf.generatePublic(spec2);
				System.out.println(publicKey);
				System.out.print(id);
				System.out.println("Input has been successfully taken.");
				publicKeyStorage.put(id, publicKey);
				
				}
	
				
				if(id.equals("0"+'\n')) {
					
					DataInputStream inID1 = new DataInputStream(socket.getInputStream());
					id = (inID1.readLine()+ '\n');
					System.out.println(id + "geldi VSDAN" );
				
					ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
					VSKeyOutputStream.writeObject(publicKeyStorage.get(id).getEncoded());
					System.out.println("KEY GITTI");
					
				}
				else {
						ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
						VSKeyOutputStream.writeObject(publicKeyStorage.get("0"+'\n').getEncoded());
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 finally {	
			 		try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		         }
		
	}

	
}
