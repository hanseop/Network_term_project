
package mafia_test;

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
	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JTextField textField = new JTextField(20);
	JTextArea messageArea = new JTextArea(4, 40);
	public String[] vote_name = new String[7];

	public Send_socket() {
		messageArea.setEditable(false);
		textField.setEditable(false);
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

	/* 게임에서 사용할 이름을 입력받음 */
	private String getsName() {
		return JOptionPane.showInputDialog(frame, "Choose a User's nikname:", "Who is the mafia",
				JOptionPane.PLAIN_MESSAGE);
	}

	/* 아래 run 함수의 int page는 메인화면에서 입장할 때 만 유저의 닉네임을 받고 싶어 만든 변수입니다. */
	void runChat(String[] players, int page) throws IOException {
		// Make connection and initialize streams
		String serverAddress = new String(getServerAddress());
		Socket socket = new Socket(serverAddress, 9001);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		/* 아래 while문은 게임 내 프로토콜에서 KICKED되지 않으면 GUI가 영원히 종료 안 되는 문제가 있습니다.. */

		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getsName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(8) + "\n");
			}
			// if(서버가 5분이 되었다고 알려주면)

			// out.println("vote"+vote(players)); //-> players는 유저이름이 담긴 스트링 배열
			else if (line.startsWith("JOB")) {
				line = line.substring(3);
				String selected = police(line);
				System.out.println("police" + selected);
				out.println("/is_he_mafia?" + selected);
			} else if (line.startsWith("IS_MAFIA?")) {
				messageArea.append(line.substring(9) + "\n");
			} else if (line.startsWith("VOTENAME ")) {// 테스트를 위해 돌아가는 부분
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
			} else if (line.startsWith("KICKED")) {
				System.exit(0);
			}
		}
	}

	public String vote(String line) { // 테스트용
		String candidate = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// 투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.

		candidate = (String) JOptionPane.showInputDialog(null, "누구를 죽이시겠습니까?", "vote", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null에는 이 팝업을 띄울 pane의 이름을 적는다.
		return candidate; // ->서버에게 candidate를 리턴함.
	}

	public String police(String line) { // 테스트용
		String selected = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// 투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.

		selected = (String) JOptionPane.showInputDialog(null, "누구의 직업이 궁금하신가요?", "select", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null에는 이 팝업을 띄울 pane의 이름을 적는다.
		return selected;
	}

	public String doctor(String line) {
		String protect = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// 투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.

		protect = (String) JOptionPane.showInputDialog(null, "누구를 지키실 건가요?", "protect", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null에는 이 팝업을 띄울 pane의 이름을 적는다.
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