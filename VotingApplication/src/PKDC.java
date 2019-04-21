
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PKDC {
	
	public static void main(String[] args) {
		ServerSocket serverSocket=null;
		Socket socket=null;
	
		try {
			serverSocket = serverSocket = new ServerSocket(9961);
		} catch (IOException e1) {
			System.out.println("port is full");
		};
		ExecutorService threads = Executors.newFixedThreadPool(10);

		while (true) {
			try {		
				socket = serverSocket.accept();
				System.out.println("servere baðlandi");
				PKDCThread PKDCthr = new PKDCThread(socket);
				threads.execute(PKDCthr);
			} catch (IOException e) {
				System.out.println("Connection fail");
				System.exit(-1);
			}
		
			
		}
		
		
		
	}
}
