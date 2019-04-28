package project_VOTE;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
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
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.ButtonGroup;

//============================================================================
//Name        : Voting.java
//============================================================================
//Voting Booth (VB) application to cast the vote and send it to Voting Server. 
//The tasks and/or objectives accomplished in this class are:
//  1. sign and encrypt the vote
//  2. send it to the voting server.

public class Voting {

	private JFrame frame;
	private ButtonGroup buttonGroup;
	private Socket votingSocket; 
	private String party ;
	private Cipher cipher;
	private byte [] encrypted_text;
	private byte[] plain_text;
	private String id;
	private byte[] encodedVSPK;
	private byte[] encrypted_hash;
	public void vote() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					System.out.println("JFrame issue in Voting.");
				}
			}
		});
	}

	public Voting(byte[] encodedVSPK,String id) {
		this.id=id;
		this.encodedVSPK=encodedVSPK;
        initialize();
        
    }

	

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		buttonGroup = new ButtonGroup();
		JLabel lblPartyA = new JLabel("Party A");
		lblPartyA.setBounds(77, 73, 69, 39);
		frame.getContentPane().add(lblPartyA);
		
		JLabel lblPartyB = new JLabel("Party B");
		lblPartyB.setBounds(194, 73, 69, 39);
		frame.getContentPane().add(lblPartyB);
		
		JLabel lblPartyC = new JLabel("Party C");
		lblPartyC.setBounds(315, 73, 69, 39);
		frame.getContentPane().add(lblPartyC);
		
		JLabel lblPartyD = new JLabel("Party D");
		lblPartyD.setBounds(443, 73, 69, 39);
		frame.getContentPane().add(lblPartyD);
		
		JRadioButton PartyARadioButton = new JRadioButton("");
		PartyARadioButton.setBounds(87, 119, 26, 23);
		frame.getContentPane().add(PartyARadioButton);
		
		JRadioButton PartyBRadioButton = new JRadioButton("");
		PartyBRadioButton.setBounds(204, 119, 26, 23);
		frame.getContentPane().add(PartyBRadioButton);
		
		JRadioButton PartyCRadioButton = new JRadioButton("");
		PartyCRadioButton.setBounds(325, 119, 26, 23);
		frame.getContentPane().add(PartyCRadioButton);
		
		JRadioButton PartyDRadioButton = new JRadioButton("");
		PartyDRadioButton.setBounds(453, 119, 26, 23);
		frame.getContentPane().add(PartyDRadioButton);
		buttonGroup.add(PartyARadioButton);
		buttonGroup.add(PartyBRadioButton);
		buttonGroup.add(PartyCRadioButton);
		buttonGroup.add(PartyDRadioButton);
		
		JButton voteButton = new JButton("Vote");
		voteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(PartyARadioButton.isSelected()) {
					party="PartyA";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyBRadioButton.isSelected()) {
					party="PartyB";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyCRadioButton.isSelected()) {
					party="PartyC";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyDRadioButton.isSelected()) {
					party="PartyD";
					sendVote(party,encodedVSPK,id);
				}
			}
		});
		voteButton.setBounds(233, 187, 120, 39);
		frame.getContentPane().add(voteButton);
		frame.setVisible(true);
	}
	
	//Send vote to Voting Server.
	public void sendVote(String party,byte [] encodedVSPK,String id) {
		Statement stmt = null;
		Connection connection=null;
		ResultSet checkSignature=null;
		try {
			Class.forName("org.sqlite.JDBC");	
			connection = DriverManager.getConnection("jdbc:sqlite:Voters.db");
			stmt=connection.createStatement();
			checkSignature = stmt.executeQuery("SELECT `voteisdone`FROM users WHERE `TC`= ('"+ id +"')");
		} catch (ClassNotFoundException e1) {
			System.out.println("Couldn't reach the class with the specified name.(Voting)");
		} catch (SQLException e) {
			System.out.println("Database access issues.(Voting)");
		}
		
		
		
        try {
        	//Check Signature of user,if it doesn't exists on the database,enable voting.
			if (checkSignature.getString(1)==null) {
				try {
					// Read users encoded Private Key from a file.Generate the Private Key.
					byte[] keyBytes = Files.readAllBytes(new File("KeyStore" + id + "/privateKey").toPath());
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
					KeyFactory kf = KeyFactory.getInstance("RSA");
					PrivateKey privateKey = kf.generatePrivate(spec);

					//Gets the encoded Voting Server Public Key and generate the Voting Server public key.
					X509EncodedKeySpec spec2 = new X509EncodedKeySpec(encodedVSPK);
					PublicKey publicKey = kf.generatePublic(spec2);

					//Get a Cipher for the desired transformation and choose the method.
					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.ENCRYPT_MODE, publicKey);
					//Perform the encryption operation.
					plain_text = party.getBytes("UTF-8");
					encrypted_text = cipher.doFinal(plain_text);

					// Message Digest
					MessageDigest md = MessageDigest.getInstance("SHA-512");
					byte[] hashInBytes = md.digest(encrypted_text);
					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.ENCRYPT_MODE, privateKey);
					encrypted_hash = cipher.doFinal(hashInBytes);
					
					// Creating Signature
					Signature rsa = Signature.getInstance("SHA512withRSA");
					//Sign the data using the private key
					rsa.initSign(privateKey);
					rsa.update(encrypted_text);
					byte[] signature = rsa.sign();
						
					//Updates the database, adds signature.
					stmt.executeUpdate("UPDATE `users` SET `voteisdone`=('" + signature + "') WHERE `TC`=('" + id + "')");
					votingSocket = new Socket("localhost", 4214);
					
					//EncryptedText sends the encrypted vote to the Voting Server.
					ObjectOutputStream EncryptedText = new ObjectOutputStream(votingSocket.getOutputStream());
					EncryptedText.writeObject(encrypted_text);
					EncryptedText.flush();
					
					//SignaturedData sends signature to the Voting Server
					ObjectOutputStream SignaturedData = new ObjectOutputStream(votingSocket.getOutputStream());
					SignaturedData.writeObject(signature);
					SignaturedData.flush();

					//EncryptedHash sends Hash to the Voting Server
					ObjectOutputStream EncryptedHash = new ObjectOutputStream(votingSocket.getOutputStream());
                    EncryptedHash.writeObject(encrypted_hash);
                    EncryptedText.flush();

                    //idOutput sends unique user id to the Voting Server
					DataOutputStream idOutput = new DataOutputStream(votingSocket.getOutputStream());
					idOutput.writeBytes(id + '\n');
					
					JOptionPane.showMessageDialog(frame, "Vote is successfully sent.");
					frame.setVisible(false);
					votingSocket.close();
					connection.close();
					System.exit(-1);

				} catch (NoSuchAlgorithmException e) {
					System.out.println("Specified algorithm is invalid.(Voting)");
				} catch (InvalidKeySpecException e) {
					System.out.println("Invalid Key specification.(Voting)");					
				} catch (NoSuchPaddingException e) {
					System.out.println("Padding mechanism is not available.(Voting)");
				} catch (InvalidKeyException e) {
					System.out.println("Key is invalid.(Voting)");
				} catch (IllegalBlockSizeException e) {
					System.out.println("Block cipher is incorrect.(Voting)");
				} catch (BadPaddingException e) {
					System.out.println("Data is not padded properly.(Voting)");
				} catch (SignatureException e) {
					System.out.println("Signature exception.(Voting)");
				} catch (UnknownHostException e) {
					System.out.println("The IP address of a host could not be determined.(Voting)");
				} catch (IOException e) {
					System.out.println("IO operation errors.(Voting)");
				} 
			}
			else{
				//Check Signature of user,if it already exists on the database,disable voting.
				
				JOptionPane.showMessageDialog(frame, "You can't vote twice");
				frame.setVisible(false);
				connection.close();
				System.exit(-1);
			}
		} catch (SQLException e) {
			System.out.println("Database access issues.(Voting)");
		}
       
    }
		
}