package project_VOTE;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
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
		lock.lock();
			try {

				ObjectInputStream inPublicKey=new ObjectInputStream(socket.getInputStream());
				byte[] b = (byte[]) inPublicKey.readObject();
				X509EncodedKeySpec spec2 = new X509EncodedKeySpec(b);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = kf.generatePublic(spec2);
				System.out.println(publicKey);
				
				BufferedReader inID = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				id = (inID.readLine()+ '\n');
				System.out.print(id);
				System.out.println("Input has been successfully taken.");

				publicKeyStorage.put(id, publicKey);
				
				
				
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
		        	 lock.unlock();
		         }
		
	}

	
}
