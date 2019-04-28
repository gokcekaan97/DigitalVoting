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
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.crypto.Cipher;
import javax.swing.ButtonGroup;

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
					e.printStackTrace();
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
					party="Party1";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyBRadioButton.isSelected()) {
					party="Party2";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyCRadioButton.isSelected()) {
					party="Party3";
					sendVote(party,encodedVSPK,id);
				}
				else if(PartyDRadioButton.isSelected()) {
					party="Party4";
					sendVote(party,encodedVSPK,id);
				}
			}
		});
		voteButton.setBounds(233, 187, 120, 39);
		frame.getContentPane().add(voteButton);
		frame.setVisible(true);
	}
	
	
	public void sendVote(String party,byte [] encodedVSPK,String id) {
		Statement stmt = null;
		Connection connection=null;
		ResultSet checkSigniture=null;
		try {
			Class.forName("org.sqlite.JDBC");	
			connection = DriverManager.getConnection("jdbc:sqlite:Voters.db");
			stmt=connection.createStatement();
			checkSigniture = stmt.executeQuery("SELECT `voteisdone`FROM users WHERE `TC`= ('"+ id +"')");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
        try {
			if (checkSigniture.getString(1)==null) {
				try {

					byte[] keyBytes = Files.readAllBytes(new File("KeyStore" + id + "/privateKey").toPath());
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
					KeyFactory kf = KeyFactory.getInstance("RSA");
					PrivateKey privateKey = kf.generatePrivate(spec);

					//VOTING SERVERIN KEYIYLE SIFRELEDIK
					X509EncodedKeySpec spec2 = new X509EncodedKeySpec(encodedVSPK);
					PublicKey publicKey = kf.generatePublic(spec2);

					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.ENCRYPT_MODE, publicKey);

					plain_text = party.getBytes("UTF-8");
					encrypted_text = cipher.doFinal(plain_text);//şifrelenmiş veri

					MessageDigest md = MessageDigest.getInstance("SHA-512");
					byte[] hashInBytes = md.digest(encrypted_text);
					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.ENCRYPT_MODE, privateKey);
					encrypted_hash = cipher.doFinal(hashInBytes);
					
					//signature
					Signature rsa = Signature.getInstance("SHA512withRSA");
					rsa.initSign(privateKey);
					rsa.update(encrypted_text);
					byte[] signature = rsa.sign();
						
					stmt.executeUpdate("UPDATE `users` SET `voteisdone`=('" + signature + "') WHERE `TC`=('" + id + "')");
					System.out.println(encrypted_text);
					votingSocket = new Socket("localhost", 4214);
					ObjectOutputStream EncryptedText = new ObjectOutputStream(votingSocket.getOutputStream());
					EncryptedText.writeObject(encrypted_text);
					EncryptedText.flush();
					
					ObjectOutputStream SignaturedData = new ObjectOutputStream(votingSocket.getOutputStream());
					SignaturedData.writeObject(signature);
					SignaturedData.flush();

					ObjectOutputStream EncryptedHash = new ObjectOutputStream(votingSocket.getOutputStream());
                    EncryptedHash.writeObject(encrypted_hash);
                    EncryptedText.flush();

					DataOutputStream idOutput = new DataOutputStream(votingSocket.getOutputStream());
					idOutput.writeBytes(id + '\n');
					
					JOptionPane.showMessageDialog(frame, "Vote is successfully sent.");
					frame.setVisible(false);
					votingSocket.close();
					connection.close();
					System.exit(-1);

				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			else{
				JOptionPane.showMessageDialog(frame, "You can't vote twice");
				frame.setVisible(false);
				votingSocket.close();
				connection.close();
				System.exit(-1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }
		
}