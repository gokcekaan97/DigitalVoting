package project_VOTE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class VotingServerThread implements Runnable {
	private Socket PKDCsocket;
	private Socket Votingsocket;
	private String id = null;
	private byte[] byteVotingServerPrivateKey;
	private PublicKey VoterpublicKey;
	private Cipher cipher;
	private byte[] Vote;
	private PrivateKey privateKey;
	private final static Lock lock = new ReentrantLock();

	public VotingServerThread(Socket Votingsocket, Socket PKDCsocket, byte[] VSprivateKey) {
		this.byteVotingServerPrivateKey = VSprivateKey;
		this.PKDCsocket = PKDCsocket;
		this.Votingsocket = Votingsocket;
	}

	public void run() {
		lock.lock();
		try {
			ObjectInputStream EncryptedVote = new ObjectInputStream(Votingsocket.getInputStream());
			byte[] Encrypted_Vote = (byte[]) EncryptedVote.readObject();
			ObjectInputStream Signature = new ObjectInputStream(Votingsocket.getInputStream());
			byte[] VoterSignature = (byte[]) Signature.readObject();

			DataInputStream inID = new DataInputStream(Votingsocket.getInputStream());
			id = (inID.readLine() + '\n');
			System.out.println(id);

			System.out.println("id nerde");
			DataOutputStream idOutput = new DataOutputStream(PKDCsocket.getOutputStream());
			idOutput.writeBytes(id + '\n');

			ObjectInputStream inVoterPublicKey = new ObjectInputStream(PKDCsocket.getInputStream());
			byte[] VoterPublicKey = (byte[]) inVoterPublicKey.readObject();
			X509EncodedKeySpec spec2 = new X509EncodedKeySpec(VoterPublicKey);
			KeyFactory kf;
			try {
				kf = KeyFactory.getInstance("RSA");
				VoterpublicKey = kf.generatePublic(spec2);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (equal(Encrypted_Vote, VoterSignature)) {
				
				
				try {
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(byteVotingServerPrivateKey);
			        kf = KeyFactory.getInstance("RSA");
			        try {
			        	privateKey = kf.generatePrivate(spec);
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
					Vote = cipher.doFinal(Encrypted_Vote);
					
					System.out.println(new String(Vote));
				//	String msgBase64 = Base64.getEncoder().encodeToString(Vote);
				//	System.out.println("Base64 Encoded String (Basic) :" + msgBase64);
					
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();

		}
	}

	public boolean equal(byte[] Encrypted_Vote, byte[] VoterSignature) {
		byte[] decrypted_text;
		byte[] hashInBytes;
		String DecryptedSigniture = null;
		String EncryptedHashedVote = null;
		try {

			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, VoterpublicKey);
			decrypted_text = cipher.doFinal(VoterSignature);

			StringBuilder sb = new StringBuilder();
			for (byte b : decrypted_text) {
				sb.append(String.format("%02x", b));
			}
			DecryptedSigniture = sb.toString();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("SHA-512");
			hashInBytes = md.digest(Encrypted_Vote);

			StringBuilder sb2 = new StringBuilder();
			for (byte b : hashInBytes) {
				sb2.append(String.format("%02x", b));
			}
			EncryptedHashedVote = sb2.toString();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return EncryptedHashedVote.equals(DecryptedSigniture);
	}

}