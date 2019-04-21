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

public class VotingServer {
	private static PublicKey publicKey;
	private static KeyPairGenerator keyGen;
	private	static KeyPair pair;
	private	static PrivateKey privateKey;
	private static Socket PKDCsocket=null;
	public static void main (String args[]) {

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
			PKDCsocket = new Socket("localhost",9951);
			DataOutputStream idOutput = new DataOutputStream(PKDCsocket.getOutputStream());
			idOutput.writeBytes("0"+'\n');
			
			ObjectOutputStream AdminOutputStream= new ObjectOutputStream(PKDCsocket.getOutputStream());
			AdminOutputStream.writeObject(publicKey.getEncoded());
			AdminOutputStream.flush();
			
			DataOutputStream idOutput2 = new DataOutputStream(PKDCsocket.getOutputStream());
			idOutput2.writeBytes("0"+'\n');
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			PKDCsocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	/*	ServerSocket VotingServerSocket=null;
		try {
			VotingServerSocket= new ServerSocket(7763);
		} catch (IOException e1) {
			System.out.println("Port is full.");
		};*/
	}
}
