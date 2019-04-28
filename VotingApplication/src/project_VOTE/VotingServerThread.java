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
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;


public class VotingServerThread implements Runnable {
	private Socket PKDCsocket;
	private Socket Votingsocket;
	private String id;
	private byte[] byteVotingServerPrivateKey;
	private PublicKey VoterpublicKey;
	private Cipher cipher;
	private byte[] Vote;
	private PrivateKey privateKey;
	private ResultSet VoteCount;
	private final static Lock lock = new ReentrantLock();
	private Map<String, byte[]> sign = Collections.synchronizedMap(new HashMap<String, byte[]>());
	private JFrame frame;

	public VotingServerThread(Socket Votingsocket, Socket PKDCsocket, byte[] VSprivateKey, Map<String, byte[]> sign) {
		this.byteVotingServerPrivateKey = VSprivateKey;
		this.PKDCsocket = PKDCsocket;
		this.Votingsocket = Votingsocket;
		this.sign = sign;
	}

	public void run() {
		lock.lock();
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:VoteRepository.db");
			Statement stmt = connection.createStatement();

			// EncryptedVote gets the users encrypted vote.
			ObjectInputStream EncryptedVote = new ObjectInputStream(Votingsocket.getInputStream());
			byte[] Encrypted_Vote = (byte[]) EncryptedVote.readObject();

			// Signature gets the users signature.
			ObjectInputStream Signature = new ObjectInputStream(Votingsocket.getInputStream());
			byte[] VoterSignature = (byte[]) Signature.readObject();

			// VHData gets the voters hashed encrypted vote
			ObjectInputStream VHData = new ObjectInputStream(Votingsocket.getInputStream());
			byte[] VoterHashData = (byte[]) VHData.readObject();

			// inID gets the voters id.
			DataInputStream inID = new DataInputStream(Votingsocket.getInputStream());
			id = (inID.readLine() + '\n');

			// idOutput sends the id of user to the PKDC
			DataOutputStream idOutput = new DataOutputStream(PKDCsocket.getOutputStream());
			idOutput.writeBytes(id + '\n');

			// inVoterPublicKey gets the voters public key from PKDC.
			ObjectInputStream inVoterPublicKey = new ObjectInputStream(PKDCsocket.getInputStream());
			byte[] VoterPublicKey = (byte[]) inVoterPublicKey.readObject();
			X509EncodedKeySpec spec2 = new X509EncodedKeySpec(VoterPublicKey);
			KeyFactory kf;
			try {
				kf = KeyFactory.getInstance("RSA");
				VoterpublicKey = kf.generatePublic(spec2);
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Specified algorithm is invalid.(VotingServerThread)");
			} catch (InvalidKeySpecException e) {
				System.out.println("Invalid key specifications.(VotingServerThread)");
			}

			// if equal method returns true vote will be counted.
			if (equal(Encrypted_Vote, VoterSignature, VoterHashData)) {
				//Protection for double voting.
				if (!sign.containsKey(id)) {
					sign.put(id, VoterSignature);
					try {
						PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(byteVotingServerPrivateKey);
						kf = KeyFactory.getInstance("RSA");
						try {
							privateKey = kf.generatePrivate(spec);
						} catch (InvalidKeySpecException e) {
							System.out.println("Invalid key specifications.(VotingServerThread)");
						}

						cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.DECRYPT_MODE, privateKey);
						Vote = cipher.doFinal(Encrypted_Vote);

						String partyName = new String(Vote);
						VoteCount = stmt
								.executeQuery("Select VoteCount From Vote Where PartyName=('" + partyName + "')");

						int temp;
						if (VoteCount.next()) {
							temp = VoteCount.getInt("VoteCount");
							temp = temp + 1;
							stmt.executeUpdate(
									"Update Vote Set VoteCount=('" + temp + "') WHERE PartyName=('" + partyName + "')");
						}

					} catch (InvalidKeyException e) {
						System.out.println("Invalid keys.(VotingServerThread)");
					} catch (NoSuchAlgorithmException e) {
						System.out.println("Specified algorithm is invalid.(VotingServerThread)");
					} catch (NoSuchPaddingException e) {
						System.out.println("Padding mechanism is not available.(VotingServerThread)");
					} catch (IllegalBlockSizeException e) {
						System.out.println("Block cipher is incorrect.(VotingServerThread)");
					} catch (BadPaddingException e) {
						System.out.println("Data is not padded properly.(VotingServerThread)");
					}

				} 
				connection.close();
			}

		} catch (IOException e) {
			System.out.println("IO operation errors.(VotingServerThread)");
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't reach the class with the specified name.(VotingServerThread)");
		} catch (SQLException e1) {
			System.out.println("Database access issues.(VotingServerThread)");
		} finally {
			// Ensure that the lock is released.
			lock.unlock();

		}
	}

	public synchronized boolean equal(byte[] Encrypted_Vote, byte[] VoterSignature, byte[] VoterHashData) {
		byte[] decrypted_text = null;
		byte[] hashInBytes;
		String DecryptedSignature = null;
		String HashedEncryptedVote = null;
		try {

			try {
				// Initialize the signature with the Public Key,
				// Update the data to be verified and then verify them using the signature
				decrypted_text = VoterSignature;
				Signature sig = Signature.getInstance("SHA512withRSA");
				sig.initVerify(VoterpublicKey);
				sig.update(Encrypted_Vote);

				// If sig.verify() returns true,decrypt the hashed date.
				if (sig.verify(VoterSignature)) {
					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.DECRYPT_MODE, VoterpublicKey);
					decrypted_text = cipher.doFinal(VoterHashData);
				}

				// bytes to hex
				StringBuilder sb = new StringBuilder();
				for (byte b : decrypted_text) {
					sb.append(String.format("%02x", b));
				}
				DecryptedSignature = sb.toString();

			} catch (SignatureException e) {
				System.out.println("Signature exception.(From equal method in VotingServerThread. )");
			} catch (NoSuchPaddingException e) {
				System.out.println("Specified algorithm is invalid.(From equal method in VotingServerThread.)");
			} catch (IllegalBlockSizeException e) {
				System.out.println("Block cipher is incorrect.(From equal method in VotingServerThread.)");
			} catch (BadPaddingException e) {
				System.out.println("Data is not padded properly.(From equal method in VotingServerThread.)");
			}

		} catch (NoSuchAlgorithmException e) {
			System.out.println("Specified algorithm is invalid.(From equal method in VotingServerThread.)");
		} catch (InvalidKeyException e) {
			System.out.println("Invalid keys.(From equal method in VotingServerThread.)");
		}
		MessageDigest md;

		try {
			// Hash the encrypted vote.
			md = MessageDigest.getInstance("SHA-512");
			hashInBytes = md.digest(Encrypted_Vote);

			// bytes to hex
			StringBuilder sb2 = new StringBuilder();
			for (byte b : hashInBytes) {
				sb2.append(String.format("%02x", b));
			}
			HashedEncryptedVote = sb2.toString();

		} catch (NoSuchAlgorithmException e) {
			System.out.println("Specified algorithm is invalid.(From equal method in VotingServerThread.)");
		}

		// if hashed encrypted vote is equal to decrypted Signature it returns true.
		return HashedEncryptedVote.equals(DecryptedSignature);
	}

}