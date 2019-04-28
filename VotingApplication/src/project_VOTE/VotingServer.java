package project_VOTE;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap; 
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//============================================================================
//Name        : VotingServer.java
//============================================================================
//A multithreaded server VotingServer that receives the votes write it to a database and display results.
//The tasks and/or objectives accomplished in this class are:
//  1. multithreaded
//  2. responsible for receiving the votes and counting them
//  3. verifying the vote is cast legitimately and singularly
//4. storing the vote in the database
public class VotingServer {
	private static PublicKey publicKey;
	private static KeyPairGenerator keyGen;
	private	static KeyPair pair;
	private	static PrivateKey privateKey;
	private static Socket PKDCsocket=null;
	private static Socket Votingsocket=null;
	private static ServerSocket VotingServerSocket=null;
	private static boolean check =false;
	private static Map <String,byte[]> sign=new HashMap<String,byte[]>();;
	public static void main (String args[]) {
		ExecutorService threads = Executors.newFixedThreadPool(10);
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm not found!");
		}
		keyGen.initialize(1024);
		pair = keyGen.generateKeyPair();
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();		
	 
		try {
			//Create a port for Voting Server.
            VotingServerSocket= new ServerSocket(4214);
            while (true) {
            try {
        			PKDCsocket = new Socket("localhost",9951);
        			//Sends the Voting Server id to PKDC.
        			DataOutputStream idOutput = new DataOutputStream(PKDCsocket.getOutputStream());
        			idOutput.writeBytes("0"+'\n');
        			idOutput.flush();
        			
        			//Ensure that the voting server public key is sent only once to PKDC. 
        			if(!check) {
        			ObjectOutputStream AdminOutputStream= new ObjectOutputStream(PKDCsocket.getOutputStream());
        			AdminOutputStream.writeObject(publicKey.getEncoded());
        			AdminOutputStream.flush();
        			check=true;
            		}
        		} catch (IOException e) {
        			System.out.println("IO operation errors.(VotingServer)");
        	}
            try {
                Votingsocket = VotingServerSocket.accept();
                System.out.println("Connection established.");
                
                //Supports multiple user by multithreading.
                VotingServerThread VSthr = new VotingServerThread(Votingsocket,PKDCsocket,privateKey.getEncoded(),sign);
                threads.execute(VSthr);
            } catch (IOException e) {
                System.out.println("Connection failed.");
                System.exit(-1);
            }
        }
        } catch (IOException e1) {
            System.out.println("Port is full.");
        };
		

	}
}
