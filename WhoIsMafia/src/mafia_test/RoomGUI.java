package mafia_test;

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

public class RoomGUI implements Runnable {
	JButton[] button = new JButton[50];
	int index = 0;
	static JFrame frame = new JFrame("Who is Mafia?");
	JFrame actionFrame = new JFrame();
	ImageIcon room = new ImageIcon("room2.jpg");
	Image newImage = room.getImage();
	Image changedImage = newImage.getScaledInstance(790, 600, Image.SCALE_SMOOTH);
	ImageIcon newRoom = new ImageIcon(changedImage);

	Send_socket chat = new Send_socket();
	JPanel panel = new JPanel() {
		public void paintComponent(Graphics g) {
			g.drawImage(newRoom.getImage(), 0, 0, null);
		}
	};

	public void runGUI() {

		frame.setBounds(0, 0, 1250, 720);
		frame.getContentPane().add(panel);
		ImageIcon key0 = new ImageIcon("key.png");
		ImageIcon key1 = new ImageIcon("key.png");
		ImageIcon key2 = new ImageIcon("key.png");
		ImageIcon key3 = new ImageIcon("key.png");
		ImageIcon key4 = new ImageIcon("key.png");
		ImageIcon key5 = new ImageIcon("key.png");
		ImageIcon key6 = new ImageIcon("key.png");
		ImageIcon key7 = new ImageIcon("key.png");
		ImageIcon key8 = new ImageIcon("key.png");
		ImageIcon key9 = new ImageIcon("key.png");

		this.setObject(key0, 610, 260);
		this.setObject(key1, 210, 360);
		this.setObject(key2, 400, 450);
		this.setObject(key3, 170, 450);
		this.setObject(key4, 380, 120);
		this.setObject(key5, 185, 20);
		this.setObject(key6, 100, 160);
		this.setObject(key7, (int) (Math.random() * 190) + 210, (int) (Math.random() * 570));
		this.setObject(key8, (int) (Math.random() * 490) + 210, (int) (Math.random() * 50) + 520);
		this.setObject(key9, (int) (Math.random() * 380) + 320, (int) (Math.random() + 20) + 150);
	}

	public void setObject(ImageIcon object, int x, int y) {

		panel.setLayout(null);
		// Insets insets = panel.getInsets();
		// System.out.println(ran1 +" " + ran2);
		button[index] = new JButton(object);

		Dimension size = button[0].getPreferredSize();
		button[index].setBackground(Color.red);
		button[index].setBorderPainted(false);
		button[index].setFocusPainted(false);
		button[index].setContentAreaFilled(false);
		JButton job = new JButton("JOB");
		JButton story = new JButton("Game Story");
		job.getPreferredSize();
		story.getPreferredSize();
		panel.add(job);
		panel.add(story);
		job.setBounds(690, 600, 100, 50);
		story.setBounds(590, 600, 100, 50);
		job.addActionListener(new Handler());
		story.addActionListener(new storyHandler());
		panel.add(button[index]);
		button[index].setBounds(x, y, 45, 20);
		panel.setVisible(true);
		frame.setVisible(true);

		button[index].setEnabled(false);
		button[index].addActionListener(new TheHandler());
		index++;
	}

	private class TheHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int i = 0; i < 50; i++) {
				if (event.getSource() == button[i])
					Send_socket.out.println("object_clicked" + i);
			}
		}
	}

	private class Handler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Send_socket.out.println("/job");
		}
	}

	private class storyHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Send_socket.out.println("/story");
		}
	}

	public void run() {
		this.runGUI();
	}

}