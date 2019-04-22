package project_VOTE;
import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.JList;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Canvas;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.ButtonGroup;

public class Voting {

	private JFrame frame;
	private ButtonGroup buttonGroup;
	private Socket votingSocket; 
	

	
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

	public Voting(byte[] publicKey) {
        initialize(publicKey);
    }

	public void sendVote() {
        try {
            votingSocket = new Socket("localhost", 4214);
            frame.setVisible(false);
            votingSocket.close();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
	

	private void initialize(byte[] publicKey) {
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
					sendVote();
				}
				else if(PartyBRadioButton.isSelected()) {
					sendVote();
				}
				else if(PartyCRadioButton.isSelected()) {
					sendVote();
				}
				else if(PartyDRadioButton.isSelected()) {
					sendVote();
				}
			}
		});
		voteButton.setBounds(233, 187, 120, 39);
		frame.getContentPane().add(voteButton);
		frame.setVisible(true);
	}
	
	
}