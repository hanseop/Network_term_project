/*opening*/
/*기존의 Layout class*/
import net_hw2.GUItemplate;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import java.util.*;
/*클래스이름이 변경되었으므로 클래스 불러오는 코드 꼭! 수정해야함.*/
public class testClient_layout extends JFrame {
	JTextField textField = new JTextField(80);
	JTextArea messageArea = new JTextArea(16, 80);
	Container frm;
	RoomGUI gui = new RoomGUI();
	JFrame frame = new JFrame();
	Send_socket chat = new Send_socket();
	
	 public testClient_layout() {
	  setTitle("who is the MAFIA");
	  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  ImageIcon background = new ImageIcon("opening.png");
	  
	  frm = new JFrame("who is the MAFIA");
	  
	  frm.setBounds(0, 0, 1200, 900);
	  ImageIcon start_btt = new ImageIcon("start_button.png");
	  ImageIcon start_btt_p = new ImageIcon("start_button_pressed.png");
	  
	  JLabel imageLabel = new JLabel(background);
	  imageLabel.setBounds(0, 0, 1200, 900);
	  frm.add(imageLabel);
	  
	  JLabel btt_L = new JLabel();
	  JButton strt_btt = new JButton(start_btt);
	  strt_btt.setRolloverIcon(start_btt_p);
	  strt_btt.setPressedIcon(start_btt_p);//넣어도 되고 안 넣어도 됨
	  strt_btt.setSize(150,90);
	  strt_btt.setBorderPainted(false);
	  strt_btt.setBounds(535, 455, 150, 90);
	  btt_L.add(strt_btt);
	  strt_btt.addActionListener(new press_start());
	  frm.add(btt_L);

	  frm.setSize(1200, 900);
	  frm.setVisible(true);
	 }	
	  
	  private class press_start implements ActionListener{ 
		    public void actionPerformed(ActionEvent event){
		     try {
				chat.run(null, 1);
			  	frm.setVisible(false);
			  	gui.runGUI();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	  }		   
public static void main(String[] args) {
	 testClient_layout client = new  testClient_layout();
	client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
}
