import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
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


	
	public void vote() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//burda bi deðisiklik olcak
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public Voting() {
		initialize();
	}


	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		buttonGroup = new ButtonGroup();
		JLabel lblNewLabel = new JLabel("Party A");
		lblNewLabel.setBounds(77, 73, 69, 39);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPartyD = new JLabel("Party B");
		lblPartyD.setBounds(194, 73, 69, 39);
		frame.getContentPane().add(lblPartyD);
		
		JLabel lblPartyC = new JLabel("Party C");
		lblPartyC.setBounds(315, 73, 69, 39);
		frame.getContentPane().add(lblPartyC);
		
		JLabel lblPartyD_1 = new JLabel("Party D");
		lblPartyD_1.setBounds(443, 73, 69, 39);
		frame.getContentPane().add(lblPartyD_1);
		
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
		
		JButton button_2 = new JButton("Vote");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(PartyARadioButton.isSelected()) {
					
				}
				else if(PartyBRadioButton.isSelected()) {
					
				}
				else if(PartyCRadioButton.isSelected()) {
					
				}
				else if(PartyDRadioButton.isSelected()) {
					
				}
			}
		});
		button_2.setBounds(233, 187, 120, 39);
		frame.getContentPane().add(button_2);
	}
}