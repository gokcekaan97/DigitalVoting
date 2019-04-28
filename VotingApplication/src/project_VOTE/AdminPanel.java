package project_VOTE;

import java.awt.Color;
import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import org.jfree.chart.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

public class AdminPanel {

	private JFrame frame;
	private Connection connection;
	private Statement stmt;
	private ResultSet rs;

	/**
	 * Launch the application.
	 */
	public void adminPanel(){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					System.out.println("JFrame issue in AdminPanel");
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AdminPanel() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		DefaultCategoryDataset dataset = createDataset();

		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);
		frame.getContentPane().add(chartPanel);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.validate();

	}
	//Creates dataset for bar chart.
	private DefaultCategoryDataset createDataset() {
		DefaultCategoryDataset dataset = null;

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:VoteRepository.db");
			stmt = connection.createStatement();
			rs = stmt.executeQuery("Select * From Vote");
			dataset = new DefaultCategoryDataset();
			//Dataset values will be filled with the data from database.
			while (rs.next()) {
				dataset.setValue(rs.getInt("VoteCount"), "vote", rs.getString("PartyName"));
			}
			connection.close();
		} catch (SQLException e) {
			System.out.println("Database access issues.(AdminPanel)");
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't reach the class with the specified name.(AdminPanel)");
		}
		
		return dataset;
		
	}
	//Creates bar chart.
	private JFreeChart createChart(DefaultCategoryDataset dataset) {

		JFreeChart barChart = ChartFactory.createBarChart3D("Selection Counter", "Parties", "Vote Counts", dataset,
				PlotOrientation.VERTICAL, false, true, false);

		return barChart;
	}

}
