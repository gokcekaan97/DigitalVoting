import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminLogin {

	private JFrame frmAdminPanel;
	private JTextField textField;
	private JPasswordField passwordField;
	
	public void adminLogin() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmAdminPanel.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public AdminLogin() {
		initialize();
	}

	private void initialize() {
		frmAdminPanel = new JFrame();
		frmAdminPanel.setTitle("Admin Panel");
		frmAdminPanel.setBounds(100, 100, 450, 300);
		frmAdminPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAdminPanel.getContentPane().setLayout(null);
		
		JLabel lblUsername = new JLabel("  T.C. No:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblUsername.setBounds(52, 79, 84, 24);
		frmAdminPanel.getContentPane().add(lblUsername);
		
		textField = new JTextField();
		textField.setBounds(131, 79, 164, 26);
		frmAdminPanel.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPassword.setBounds(52, 146, 80, 24);
		frmAdminPanel.getContentPane().add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(131, 146, 164, 24);
		frmAdminPanel.getContentPane().add(passwordField);
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btnNewButton.setBounds(161, 199, 95, 24);
		frmAdminPanel.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Admin Panel");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel.setBounds(145, 27, 111, 24);
		frmAdminPanel.getContentPane().add(lblNewLabel);
	}
}