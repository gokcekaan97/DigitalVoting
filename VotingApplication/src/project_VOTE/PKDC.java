package project_VOTE;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
 

//============================================================================
//Name        : PKDC.java
//============================================================================
//A multithreaded server PKDC that serves the public keys.
//The tasks and/or objectives accomplished in this class are:
//  1. multithreaded.
//  2. responsible for storing public keys.
//  3. delivers the public keys to a requestor.
public class PKDC {
	
	public static void main(String[] args) {
		
		//Since the server never stop,we have stored the public keys on the hash map instead of a database.
		HashMap<String,PublicKey> publicKeyStorage = new HashMap<String, PublicKey>();
		ServerSocket PKDCserverSocket=null;
		Socket socket=null;
		BufferedReader inID;
		
		//Create a port for PKDC server.
		try {
			PKDCserverSocket= new ServerSocket(9951);
		} catch (IOException e1) {
			System.out.println("Port is full.");
		};
		
		while (true) {		 
			try {		
				socket = PKDCserverSocket.accept();
				System.out.println("Connection established.");
				
				//Supports multiple user by multithreading.
				PKDCThread PKDCthr = new PKDCThread(socket,publicKeyStorage);
				Thread PKDCThread = new Thread(PKDCthr);
				PKDCThread.start();
			

			} catch (IOException e) {
				System.out.println("Connection failed.");
				System.exit(-1);
			}
		}	
	}
}
