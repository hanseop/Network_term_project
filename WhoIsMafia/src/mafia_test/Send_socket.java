package net_hw2;

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

public class Send_socket implements Runnable {
	private static int matrixSize = 7;
	static BufferedReader in;
	static PrintWriter out;
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	Font font = new Font("나눔고딕", Font.PLAIN, 20);
	JTextField textField = new JTextField(22);
	JTextArea messageArea = new JTextArea(4, 22);
	public String[] vote_name = new String[7];

	public Send_socket() {
		messageArea.setEditable(false);
		textField.setEditable(false);
		messageArea.setFont(font);
		textField.setFont(font);
		RoomGUI.frame.getContentPane().add(textField, "South");
		RoomGUI.frame.getContentPane().add(new JScrollPane(messageArea), "East");
		RoomGUI.frame.setVisible(true);
		textField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
	}

	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Who is the mafia",
				JOptionPane.PLAIN_MESSAGE);
	}

	/* 유저 닉네임 입력 */
	private String getsName() {
		return JOptionPane.showInputDialog(frame, "Choose a User's nikname:", "Who is the mafia",
				JOptionPane.PLAIN_MESSAGE);
	}

	void runChat(String[] players, int page) throws IOException {
		// Make connection and initialize streams
		int[][] matrix = new int[matrixSize][matrixSize];
		String serverAddress = new String(getServerAddress());
		JFrame actionFrame = new JFrame();
		Socket socket = new Socket(serverAddress, 9001);
		Thread t3 = new Thread(new Timer_start());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getsName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(8) + "\n");
			} else if (line.startsWith("ERROR")){
				JOptionPane.showMessageDialog(null, line.substring(6));
			}

			// out.println("vote"+vote(players)); //-> players 유저 이름이 담긴 리스트
			else if (line.startsWith("JOB")) {
				line = line.substring(3);
				String selected = police(line);
				System.out.println("police" + selected);
				out.println("/is_he_mafia?" + selected);
			} else if (line.startsWith("IS_MAFIA?")) {
				messageArea.append(line.substring(9) + "\n");
				out.println("/kill");
			} else if (line.startsWith("VOTENAME ")) {//테스팅 구문
				line = line.substring(9);
				String victim = vote(line);
				System.out.println(victim);
				out.println("/victim" + victim);
			} else if (line.startsWith("KILL")) {
				line = line.substring(4);
				String victim = vote(line);
				System.out.println(victim);
				out.println("/dead" + victim);
			} else if (line.startsWith("DEAD")) {
				messageArea.append(line.substring(4) + "\n");
			} else if (line.startsWith("DOCTOR")) {
				line = line.substring(6);
				String protect = doctor(line);
				System.out.println(protect);
				out.println("/protect" + protect);
			} else if (line.startsWith("D_START")) {
				messageArea.append("\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("\t아침이 밝았다." + "\n");
				messageArea.append(line.substring(7) + "\n");
			} else if (line.startsWith("MATRIX")) {
				int count = 0;
				line = line.substring(6);
				String[] temp = line.split(" ");
				for (int i = 0; i < matrixSize; i++) {
					for (int j = 0; j < matrixSize; j++) {
						matrix[i][j] = Integer.parseInt(temp[count]);
						count++;
					}
				}
			} else if (line.startsWith("T_START")) {
				messageArea.append("\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("      증거를 충분히 찾은 것 같다." + "\n");
				messageArea.append("     사람들과 의논하여 마피아를 찾자." + "\n");
				t3.start();
				JOptionPane.showMessageDialog(actionFrame, line, "Message", 2);
			} else if (line.startsWith("V_END")) {
				messageArea.append(line.substring(5)+"\n\n");
				messageArea.append("\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("       한밤중이 되었다." + "\n");
				messageArea.append("    보안관은 의심 가는 사람을 심문한다." + "\n");
				messageArea.append("     마피아는 다음 살해 대상을 고른다." + "\n");
				messageArea.append("      의사는 한 사람을 보호한다." + "\n");
				out.println("/police");
			} else if (line.startsWith("KICKED")) {
				System.exit(0);
			}
		}

	}

	public String vote(String line) { //테스팅
		String candidate = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);//투표를 위한 유저 이름을 담는 변수

		candidate = (String) JOptionPane.showInputDialog(null, "마피아로 의심되는 사람을 지목하자.", "vote", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		return candidate; // ->서버에게 candidate 전달
	}

	public String police(String line) { //테스팅
		String selected = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);//유저 이름 저장 변수
		selected = (String) JOptionPane.showInputDialog(null, "누구를 죽일까...", "select", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		return selected;
	}

	public String doctor(String line) {
		String protect = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);//유저 이름 저장 변수

		protect = (String) JOptionPane.showInputDialog(null, "누구를 보호할까...", "protect", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		return protect;
	}

	public void run() {
		try {
			this.runChat(null, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}