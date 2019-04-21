package project_VOTE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PKDC {
	
	public static void main(String[] args) {
		HashMap<String,PublicKey> publicKeyStorage = new HashMap<String, PublicKey>();
		ServerSocket PKDCserverSocket=null;
		Socket socket=null;
		BufferedReader inID;
		//DATABASE
		
		try {
			PKDCserverSocket= new ServerSocket(9951);
		} catch (IOException e1) {
			System.out.println("Port is full.");
		};
		ExecutorService threads = Executors.newFixedThreadPool(10);

		while (true) {		
			
			try {		
				socket = PKDCserverSocket.accept();
				System.out.println("Connection established.");
				inID = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String id = (inID.readLine()+ '\n');
				
				if(publicKeyStorage.containsKey(id)==false) {
				PKDCThread PKDCthr = new PKDCThread(socket,publicKeyStorage);
				threads.execute(PKDCthr);
				}
				
				if(id.equals("0"+'\n')) {
				/*	ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
					VSKeyOutputStream.writeObject(publicKeyStorage.get(id).getEncoded()); */
				}
				else {
					ObjectOutputStream VSKeyOutputStream= new ObjectOutputStream(socket.getOutputStream());
					VSKeyOutputStream.writeObject(publicKeyStorage.get("0"+'\n').getEncoded());
				}
				
				
			} catch (IOException e) {
				System.out.println("Connection failed.");
				System.exit(-1);
			}
			System.out.println(publicKeyStorage);
		}	
	}
}
