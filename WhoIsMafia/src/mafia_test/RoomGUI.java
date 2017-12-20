package net_hw2;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.lang.Object;
/**********************************************************************************
 * The RoomGUI class shows the map of room that users play mafia game and also there are
 * textfield and textmessageArea that users can chatt each other.
 * There is a one big room map and in random area there are object button that users can click
 * if the users click object user send protocol to server and server replies object message to user
 * also there are job and story button that describe the mafia game
 **********************************************************************************/
public class RoomGUI implements Runnable {
	static JButton[] button = new JButton[50];
	static int index = 0;
	static JFrame frame = new JFrame("Who is the Mafia?");
	static JFrame actionFrame = new JFrame();
	ImageIcon room = new ImageIcon("map_2_.png");
	ImageIcon job_butt = new ImageIcon("jab.png");
	ImageIcon story_butt = new ImageIcon("story.png");
	Image newImage = room.getImage();
	Image changedImage = newImage.getScaledInstance(790, 600, Image.SCALE_SMOOTH);
	ImageIcon newRoom = new ImageIcon(changedImage);
	
	JPanel panel = new JPanel() {
		public void paintComponent(Graphics g) {
			g.drawImage(newRoom.getImage(), 0, 0, null);
		}
	};
	/*
	 * method runGUI gets imageicon value of object image
	 */
	public void runGUI() {
		frame.setBounds(0, 0, 1400, 800);
		frame.getContentPane().add(panel);
		ImageIcon key0 = new ImageIcon("scroll_dot.png");
		ImageIcon key1 = new ImageIcon("scroll_dot.png");
		ImageIcon key2 = new ImageIcon("scroll_dot.png");
		ImageIcon key3 = new ImageIcon("scroll_dot.png");
		ImageIcon key4 = new ImageIcon("scroll_dot.png");
		ImageIcon key5 = new ImageIcon("scroll_dot.png");
		ImageIcon key6 = new ImageIcon("scroll_dot.png");
		ImageIcon key7 = new ImageIcon("scroll_dot.png");
		ImageIcon key8 = new ImageIcon("scroll_dot.png");
		ImageIcon key9 = new ImageIcon("scroll_dot.png");

		this.setObject(key0, 580, 250);
		this.setObject(key1, 230, 350);
		this.setObject(key2, 420, 410);
		this.setObject(key3, 130, 480);
		this.setObject(key4, 380, 120);
		this.setObject(key5, 230, 30);
		this.setObject(key6, 150, 215);
		this.setObject(key7, (int) (Math.random() * 190) + 200, (int) (Math.random() * 500));
		this.setObject(key8, (int) (Math.random() * 450) + 200, (int) (Math.random() * 50) + 500);
		this.setObject(key9, (int) (Math.random() * 380) + 300, (int) (Math.random() + 20) + 150);
		
		JButton job = new JButton(job_butt);
		JButton story = new JButton(story_butt);
		job.setBorderPainted(false);
		story.setBorderPainted(false);
		job.getPreferredSize();
		story.getPreferredSize();
		panel.add(job);
		panel.add(story);
		job.setBounds(700, 600, 100, 50);
		story.setBounds(580, 600, 100, 50);
		job.addActionListener(new Handler());
		story.addActionListener(new storyHandler());
		
	}
	/*
	 * method setObject initalize object's random location
	 */
	public void setObject(ImageIcon object, int x, int y) {

		panel.setLayout(null);
		button[index] = new JButton(object);

		Dimension size = button[0].getPreferredSize();
		button[index].setBackground(Color.red);
		button[index].setBorderPainted(false);
		button[index].setFocusPainted(false);
		button[index].setContentAreaFilled(false);
		button[index].setPressedIcon(object);

		panel.add(button[index]);
		button[index].setBounds(x, y, 45, 45);
		panel.setVisible(true);
		frame.setVisible(true);

		//button[index].setEnabled(false);
		button[index].addActionListener(new TheHandler());
		index++;
	}

	/*
	 * handler for object button
	 */
	private class TheHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int i = 0; i < 50; i++) {
				if (event.getSource() == button[i])
					Send_socket.out.println("object_clicked" + i);
			}
		}
	}
	/*
	 * handler for job button
	 */
	private class Handler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Send_socket.out.println("/job");
		}
	}
	/*
	 * handler for story button
	 */
	private class storyHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Send_socket.out.println("/story");
		}
	}

	public void run() {
		this.runGUI();
	}

}