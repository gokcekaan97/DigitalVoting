package project_VOTE;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Pipe;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
//============================================================================
//Name        : VoterLogin.java
//============================================================================
//A client application  to create the public/private key and store the private key on a USB and send the public key to the PKDC server. 
//The tasks and/or objectives accomplished in this class are:
//  1. create public/private key pairs.
//  2. store the private key on a USB 
//  3. send the public key to the PKDC server to bind owner to the public key
public class VoterLogin {
 
	private JFrame frmVoterLoginScreen;
	private JTextField textField;
	private JPasswordField passwordField;
	private Voting voting;
	private KeyPair pair;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Socket voterPKDCSocket;
	public void voterLogin() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmVoterLoginScreen.setVisible(true);
				} catch (Exception e) {
					System.out.println("JFrame issue in VoterLogin.");
				}
			}
		});
	}
	
	public VoterLogin() {
		initialize();
	}

	private void initialize() {
		frmVoterLoginScreen = new JFrame();
		frmVoterLoginScreen.setTitle("Voter Login Screen");
		frmVoterLoginScreen.setBounds(100, 100, 450, 300);
		frmVoterLoginScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmVoterLoginScreen.getContentPane().setLayout(null);
		
		JLabel lblNationalID = new JLabel("     ID No:");
		lblNationalID.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNationalID.setBounds(52, 79, 84, 24);
		frmVoterLoginScreen.getContentPane().add(lblNationalID);
		
		textField = new JTextField();
		textField.setBounds(131, 79, 164, 26);
		frmVoterLoginScreen.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPassword.setBounds(52, 146, 80, 24);
		frmVoterLoginScreen.getContentPane().add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(131, 146, 164, 24);
		frmVoterLoginScreen.getContentPane().add(passwordField);
		
		JButton btnLoginButton = new JButton("Login");
		btnLoginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connection();
			}
		});
		btnLoginButton.setBounds(161, 199, 95, 24);
		frmVoterLoginScreen.getContentPane().add(btnLoginButton);
		
		JLabel lblVoterLabel = new JLabel("Digital Voting System");
		lblVoterLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblVoterLabel.setBounds(108, 26, 187, 24);
		frmVoterLoginScreen.getContentPane().add(lblVoterLabel);
	}
	
	//Stores the private key for a given directory.
	public static void writeToFile(String path, byte[] key) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();

		FileOutputStream fos = new FileOutputStream(f);
		fos.write(key);
		fos.flush();
		fos.close();
	}
	
	
	public void connection() {
		PreparedStatement prepstatement;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection("jdbc:sqlite:Voters.db");
			prepstatement= connection.prepareStatement("SELECT `TC`, `password` FROM `users` WHERE `TC`= ? AND `password`=?");
			prepstatement.setString(1,textField.getText());
			prepstatement.setString(2,String.valueOf(passwordField.getPassword()));
			ResultSet result=prepstatement.executeQuery();
			//If id and password exist and match,it returns true.
			if(result.next()) {
				
				voterPKDCSocket = new Socket("localhost",9951);
				frmVoterLoginScreen.setVisible(false);
				
				stmt=connection.createStatement();
				ResultSet checkPublicKey = stmt.executeQuery("SELECT `PublicKey`FROM users WHERE `TC`= ('"+ textField.getText()+"')");
				
				//idOutput sends user id to PKDC.
				DataOutputStream idOutput = new DataOutputStream(voterPKDCSocket.getOutputStream());
				idOutput.writeBytes(textField.getText()+'\n');
				idOutput.flush();
				
				//If public key doesn't exist already for user,it generates key pairs for user.
				if(checkPublicKey.getString(1)==null) {
					
					KeyPairGenerator keyGen;
					try {
						keyGen = KeyPairGenerator.getInstance("RSA");
						keyGen.initialize(1024);
						pair = keyGen.generateKeyPair();
						privateKey = pair.getPrivate();
						publicKey = pair.getPublic();
						byte [] encodedKey=publicKey.getEncoded();
						stmt.executeUpdate("UPDATE `users` SET `PublicKey`='PK is created' WHERE `TC`=('"+textField.getText()+"')" );
						writeToFile("KeyStore"+textField.getText()+"/privateKey",privateKey.getEncoded());

						//VoterOutputStream sends its public key to PKDC.
						ObjectOutputStream voterOutputStream= new ObjectOutputStream(voterPKDCSocket.getOutputStream());
						voterOutputStream.writeObject(encodedKey);
						voterOutputStream.flush();

					} catch (NoSuchAlgorithmException e) {
						System.out.println("Specified algorithm is invalid.(VoterLogin)");
					} catch (IOException e) {
						System.out.println("IO operation errors.(VoterLogin)");
					}
				}
				//It takes the Voting Server public key input from PKDC.
				ObjectInputStream inPublicKey=new ObjectInputStream(voterPKDCSocket.getInputStream());
				byte[] encodedVSPK = (byte[]) inPublicKey.readObject();
				String s = textField.getText();
				connection.close();
				//Parameterized voting object initializes voting screen.
				voting = new Voting(encodedVSPK,textField.getText());
				voting.vote();
				voterPKDCSocket.close();
			}
			else {
				//If id and password doesn't exist or match,displays an error message.
				JOptionPane.showMessageDialog(frmVoterLoginScreen, "Invalid username or password");		
			}
		}
		catch (ClassNotFoundException e1) {
			System.out.println("Couldn't reach the class with the specified name.(VoterLogin)");
		} catch (SQLException e1) {
			System.out.println("Database access issues.(VoterLogin)");
		} catch (IOException e) {
			System.out.println("IO operation errors.(VoterLogin)");
		}
	}
}