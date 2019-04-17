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
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
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


	
	
	public void voterLogin() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VoterLogin window = new VoterLogin();
					window.frmVoterLoginScreen.setVisible(true);
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
		
		JLabel lblUsername = new JLabel("  T.C. No:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblUsername.setBounds(52, 79, 84, 24);
		frmVoterLoginScreen.getContentPane().add(lblUsername);
		
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
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				connection();
			}
		});
		btnNewButton.setBounds(161, 199, 95, 24);
		frmVoterLoginScreen.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Digital Voting System");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel.setBounds(108, 26, 187, 24);
		frmVoterLoginScreen.getContentPane().add(lblNewLabel);
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
				frmVoterLoginScreen.setVisible(false);
				
				stmt=connection.createStatement();
				ResultSet publickey = stmt.executeQuery("SELECT `PublicKey`FROM users WHERE `TC`= ('"+ textField.getText()+"')");
				if(publickey.getString(1)==null) {
					
					KeyPairGenerator keyGen;
					try {
						keyGen = KeyPairGenerator.getInstance("RSA");
						keyGen.initialize(1024);
					
						KeyPair pair = keyGen.generateKeyPair();
						PrivateKey privateKey = pair.getPrivate();
						PublicKey publicKey = pair.getPublic();
						stmt.executeUpdate("UPDATE `users` SET `PublicKey`=('"+ publicKey.getEncoded() +"') WHERE `TC`=('"+textField.getText()+"')" );
						writeToFile("KeyStore/privateKey",privateKey.getEncoded());

						
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	
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