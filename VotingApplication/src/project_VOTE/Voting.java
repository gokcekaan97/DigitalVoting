package project_VOTE;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.JList;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Canvas;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
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

	public Voting(byte[] encodedVSPK) {
        initialize(encodedVSPK);
    }

	

	private void initialize(byte[] encodedVSPK) {
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
					party="party1";
					sendVote(party,encodedVSPK);
				}
				else if(PartyBRadioButton.isSelected()) {
					party="party2";
					sendVote(party,encodedVSPK);
				}
				else if(PartyCRadioButton.isSelected()) {
					party="party3";
					sendVote(party,encodedVSPK);
				}
				else if(PartyDRadioButton.isSelected()) {
					party="party4";
					sendVote(party,encodedVSPK);
				}
			}
		});
		voteButton.setBounds(233, 187, 120, 39);
		frame.getContentPane().add(voteButton);
		frame.setVisible(true);
	}
	
	
	public void sendVote(String party,byte [] encodedVSPK) {
        try {
        	byte[] keyBytes = Files.readAllBytes(new File("KeyStore/privateKey").toPath());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            //VOTING SERVERIN KEYIYLE SIFRELEDIK
            X509EncodedKeySpec spec2 = new X509EncodedKeySpec(encodedVSPK);
            PublicKey publicKey = kf.generatePublic(spec2);
        	
        	cipher = Cipher.getInstance("RSA");
        	cipher.init(Cipher.ENCRYPT_MODE, publicKey); 
        	
        	plain_text = party.getBytes("UTF-8");
        	//bu karþýya gidicek
        	encrypted_text = cipher.doFinal(plain_text);

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashInBytes = md.digest(encrypted_text);	
            
            cipher = Cipher.getInstance("RSA");
        	cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            encrypted_hash=cipher.doFinal(hashInBytes);
            
            String msgBase64 = Base64.getEncoder().encodeToString(encrypted_text);
            System.out.println("Base64 Encoded String (Basic) :" + msgBase64);
           
            votingSocket = new Socket("localhost", 4214);
            frame.setVisible(false);
            votingSocket.close();
            System.exit(-1);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
		
}