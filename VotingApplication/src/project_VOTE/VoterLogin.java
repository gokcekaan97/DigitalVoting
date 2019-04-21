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
import java.awt.event.ActionEvent;

public class VoterLogin {

	private JFrame frmVoterLoginScreen;
	private JTextField textField;
	private JPasswordField passwordField;
	private Voting voting;
	private KeyPair pair;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Socket voterSocket;
	public void voterLogin() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmVoterLoginScreen.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
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
			
			if(result.next()) {
				
				voterSocket = new Socket("localhost",9951);
				frmVoterLoginScreen.setVisible(false);
				
				stmt=connection.createStatement();
				ResultSet checkPublicKey = stmt.executeQuery("SELECT `PublicKey`FROM users WHERE `TC`= ('"+ textField.getText()+"')");
				
				DataOutputStream idOutput1 = new DataOutputStream(voterSocket.getOutputStream());
				idOutput1.writeBytes(textField.getText()+'\n');
				idOutput1.flush();
				
				if(checkPublicKey.getString(1)==null) {
					
					KeyPairGenerator keyGen;
					try {
						keyGen = KeyPairGenerator.getInstance("RSA");
						keyGen.initialize(1024);
						pair = keyGen.generateKeyPair();
						privateKey = pair.getPrivate();
						publicKey = pair.getPublic();
						System.out.println(publicKey);
						stmt.executeUpdate("UPDATE `users` SET `PublicKey`=('"+ publicKey.getEncoded() +"') WHERE `TC`=('"+textField.getText()+"')" );
						writeToFile("KeyStore/privateKey",privateKey.getEncoded());
						
					
						ObjectOutputStream voterOutputStream= new ObjectOutputStream(voterSocket.getOutputStream());
						voterOutputStream.writeObject(publicKey.getEncoded());
						voterOutputStream.flush();	
						
						DataOutputStream idOutput2 = new DataOutputStream(voterSocket.getOutputStream());
						idOutput2.writeBytes(textField.getText()+'\n');
						idOutput2.flush();
						
						
						
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				ObjectInputStream inPublicKey=new ObjectInputStream(voterSocket.getInputStream());
				byte[] b = (byte[]) inPublicKey.readObject();
				X509EncodedKeySpec spec2 = new X509EncodedKeySpec(b);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = kf.generatePublic(spec2);
				System.out.println(publicKey);
				connection.close();
				voterSocket.close();
				voting = new Voting();
				voting.vote();
			
			}
			else {
				JOptionPane.showMessageDialog(frmVoterLoginScreen, "Invalid username or password");		
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}