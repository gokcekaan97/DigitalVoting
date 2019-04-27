package project_VOTE;

import java.io.DataInputStream;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VotingServer {
	private static PublicKey publicKey;
	private static KeyPairGenerator keyGen;
	private	static KeyPair pair;
	private	static PrivateKey privateKey;
	private static Socket PKDCsocket=null;
	private static Socket Votingsocket=null;
	private static ServerSocket VotingServerSocket=null;
	private static String id;
	private static boolean check =false;
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
		//SYMMETRIC private key for admin pass 
	
	
		try {
            VotingServerSocket= new ServerSocket(4214);
            while (true) {
            try {
        			PKDCsocket = new Socket("localhost",9951);
        			
        			DataOutputStream idOutput = new DataOutputStream(PKDCsocket.getOutputStream());
        			idOutput.writeBytes("0"+'\n');
        			idOutput.flush();
        			if(!check) {
        			ObjectOutputStream AdminOutputStream= new ObjectOutputStream(PKDCsocket.getOutputStream());
        			AdminOutputStream.writeObject(publicKey.getEncoded());
        			AdminOutputStream.flush();
        			check=true;
            		}
        		} catch (IOException e) {
        			e.printStackTrace();
        	}
            try {
                Votingsocket = VotingServerSocket.accept();

                System.out.println("Connection established.");
                VotingServerThread VSthr = new VotingServerThread(Votingsocket,PKDCsocket,privateKey.getEncoded());
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
