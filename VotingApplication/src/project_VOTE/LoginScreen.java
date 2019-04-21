package project_VOTE;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

import java.awt.Font;

public class LoginScreen {
	private AdminLogin adminLogin;
	private VoterLogin voterlogin;
	private JFrame frmDigitalVotingSystem;

	public void app() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmDigitalVotingSystem.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public LoginScreen() {
		initialize();
	}

	private void initialize() {
		frmDigitalVotingSystem = new JFrame();
		frmDigitalVotingSystem.setTitle("Digital Voting System");
		frmDigitalVotingSystem.setResizable(false);
		frmDigitalVotingSystem.setBounds(100, 100, 300, 400);
		frmDigitalVotingSystem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDigitalVotingSystem.getContentPane().setLayout(null);
		
		JButton btnAdminLoginButton = new JButton("Admin Login");
		btnAdminLoginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmDigitalVotingSystem.setVisible(false);
				adminLogin = new AdminLogin();
				adminLogin.adminLogin();
			
			}
		});
		btnAdminLoginButton.setBounds(100, 116, 100, 60);
		frmDigitalVotingSystem.getContentPane().add(btnAdminLoginButton);
		
		JButton btnVoterLoginButton = new JButton("Voter Login");
		btnVoterLoginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmDigitalVotingSystem.setVisible(false);
				voterlogin = new VoterLogin();
				voterlogin.voterLogin();
				
			}
		});
		btnVoterLoginButton.setBounds(100, 208, 100, 60);
		frmDigitalVotingSystem.getContentPane().add(btnVoterLoginButton);
		
		JLabel lblVotingSystemLabel = new JLabel("Digital Voting System");
		lblVotingSystemLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblVotingSystemLabel.setBounds(69, 48, 174, 42);
		frmDigitalVotingSystem.getContentPane().add(lblVotingSystemLabel);
	}

}