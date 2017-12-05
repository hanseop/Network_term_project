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
	int x = 0, y = 0;
	static JFrame frame = new JFrame("Who is Mafia?");
	JFrame actionFrame = new JFrame();
	ImageIcon room = new ImageIcon("room2.jpg");
	Image newImage = room.getImage();
	Image changedImage = newImage.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
	ImageIcon newRoom = new ImageIcon(changedImage);

	Send_socket chat = new Send_socket();
	JPanel panel = new JPanel() {
		public void paintComponent(Graphics g) {
			g.drawImage(newRoom.getImage(), 0, 0, null);
		}
	};

	public void runGUI() {
		frame.setBounds(0, 0, 1200, 700);
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

		this.setObject(key0);
		this.setObject(key1);
		this.setObject(key2);
		this.setObject(key3);
		this.setObject(key4);
		this.setObject(key5);
		this.setObject(key6);
		this.setObject(key7);
		this.setObject(key8);
		this.setObject(key9);
	}

	public void setObject(ImageIcon object) {

		panel.setLayout(null);
		Insets insets = panel.getInsets();
		// System.out.println(ran1 +" " + ran2);
		button[index] = new JButton(object);

		Dimension size = button[0].getPreferredSize();
		button[index].setBackground(Color.red);
		button[index].setBorderPainted(false);
		button[index].setFocusPainted(false);
		button[index].setContentAreaFilled(false);

		panel.add(button[index]);
		button[index].setBounds(x, y, 50, 30);
		panel.setVisible(true);
		frame.setVisible(true);

		button[index].addActionListener(new TheHandler());
		index++;
		if (x != 700)
			x += 100;
		if (x == 700) {
			x = 0;
			y += 100;
		}

	}

	private class TheHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int i = 0; i < 50; i++) {
				if (event.getSource() == button[i])
					Send_socket.out.println("object_clicked" + i);

			}

		}
	}

	public void run() {
		this.runGUI();
	}

}