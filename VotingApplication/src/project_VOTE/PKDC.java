package project_VOTE;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;



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
		
		while (true) {		
			// THREADLER SONLANMIYOR  
			try {		
				socket = PKDCserverSocket.accept();
				System.out.println("Connection established.");
			
				PKDCThread PKDCthr = new PKDCThread(socket,publicKeyStorage);
				Thread PKDCThread = new Thread(PKDCthr);
				PKDCThread.start();
			

			} catch (IOException e) {
				System.out.println("Connection failed.");
				System.exit(-1);
			}
			System.out.println(publicKeyStorage);
		}	
	}
}
