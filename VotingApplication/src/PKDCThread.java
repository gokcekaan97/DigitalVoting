import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class PKDCThread implements Runnable{
	InputStream voterInput=null;
	private Socket socket;

	public PKDCThread(Socket socket) {
		this.socket=socket;
	}
	public void run() {
			
			try {
				voterInput = socket.getInputStream();
				System.out.println("input aldi");
				ObjectInputStream inPublicKey=new ObjectInputStream(socket.getInputStream()) ;
				byte[] b = (byte[]) inPublicKey.readObject();
				System.out.println(b);	
				X509EncodedKeySpec spec2 = new X509EncodedKeySpec(b);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PublicKey publicKey = kf.generatePublic(spec2);
				System.out.println(publicKey);
				
				
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
		
		
	}

	
}
