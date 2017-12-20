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
import java.lang.Object;

import javax.swing.*;
import java.util.*;

public class Send_socket implements Runnable {
	private static int matrixSize = 7;
	static BufferedReader in;
	static PrintWriter out;
	JPanel panel = new JPanel();
	Font font = new Font("나눔고딕", Font.PLAIN, 20);
	JTextField textField = new JTextField(30);
	JTextArea messageArea = new JTextArea(4, 30);
	public String[] vote_name = new String[7];

	static JFrame frame = new JFrame("Who is the Mafia?");
	ImageIcon flat = new ImageIcon("flat.png");
	Image newflat = flat.getImage();
	Image changedflat = newflat.getScaledInstance(790, 600, Image.SCALE_SMOOTH);
	ImageIcon newFlat = new ImageIcon(changedflat);

	JPanel panel_flat = new JPanel() {
		public void paintComponent(Graphics g) {
			g.drawImage(newFlat.getImage(), 18, 18, null);
		}
	};

	public Send_socket() {
		messageArea.setEditable(false);
		textField.setEditable(false);
		textField.setFont(font);
		messageArea.setFont(font);
		RoomGUI.frame.setBounds(0, 0, 1400, 800);
		RoomGUI.frame.getContentPane().add(panel_flat);
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
	      return JOptionPane.showInputDialog(frame, "서버의 IP주소를 입력해주세요:", "Who is the mafia", JOptionPane.PLAIN_MESSAGE);
	   }

	   /* 게임에서 사용할 이름을 입력받음 */
	   private String getsName() {
	      return JOptionPane.showInputDialog(frame, "게임에서 사용할 닉네임을 입력해주세요:", "Who is the mafia",
	            JOptionPane.PLAIN_MESSAGE);
	   }

	   void runChat(String[] players, int page) throws IOException {
	      // Make connection and initialize streams

	      String serverAddress = new String(getServerAddress());
	      JFrame actionFrame = new JFrame();
	      Socket socket = new Socket(serverAddress, 9001);
	      boolean is_kicked = false;

	      in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
	      out = new PrintWriter(socket.getOutputStream(), true);

	      while (true) {
	         String line = in.readLine();
	         if (line.startsWith("SUBMITNAME")) {
	            out.println(getsName());
	         } else if (line.startsWith("NAMEACCEPTED")) {
	            textField.setEditable(true);
	         } else if (line.startsWith("MESSAGE")) {
	            // if (line.substring(8).equals("game start"))
	            messageArea.append(line.substring(8) + "\n");
	         } else if (line.startsWith("ERROR")) {
	            JOptionPane.showMessageDialog(null, line.substring(6));
	         } else if (line.startsWith("FLAG")) {
	            panel_flat.setVisible(false);
	            this.frame.setVisible(false);
	            Thread t1 = new Thread(new RoomGUI());
	            t1.start();
	            for (int i = 0; i < RoomGUI.index; i++)
	               RoomGUI.button[i].setEnabled(true);
	         } else if (line.startsWith("ENDMESSAGE")) {
	            // if (line.substring(8).equals("game start"))
	            messageArea.append(line.substring(11) + "\n");
	            textField.setVisible(false);
	            socket.close();
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
	            out.println("/kill");
	         } else if (line.startsWith("NON")) {
	            out.println("/kill");
	         } else if (line.startsWith("VOTENAME ")) {// 테스트를 위해 돌아가는 부분
	            if (is_kicked == false) {
	               line = line.substring(9);
	               String victim = vote(line);
	               System.out.println(victim);
	               out.println("/victim" + victim);
	            }
	         } else if (line.startsWith("KILL")) {
	            line = line.substring(4);
	            String victim = mafia(line);
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
	            for (int i = 0; i < RoomGUI.index; i++)
	               RoomGUI.button[i].setEnabled(true);
	            if (is_kicked == false)
	               textField.setVisible(true);
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append("아침이 밝았습니다." + "\n");
	            messageArea.append("******************************\n");
	            messageArea.append(line.substring(7) + "\n");
	         } else if (line.startsWith("T_START")) {
	            for (int i = 0; i < RoomGUI.index; i++)
	               RoomGUI.button[i].setEnabled(false);
	            if (line.indexOf("all object selected") != -1) {
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append("충분한 조사가 이루어졌습니다." + "\n");
	               messageArea.append("밤이 되기 전에 사람들과 의논하여 마피아를 추려내세요." + "\n");
	               messageArea.append("******************************\n");
	            }
	            Thread t3 = new Thread(new Timer_start());
	            t3.start();
	         } else if (line.startsWith("V_END")) {
	            messageArea.append(line.substring(5) + "\n\n");
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append("한밤중이 되었습니다." + "\n");
	            messageArea.append("사람들은 방에 들어가 각자의 일을 하거나 취침을 합니다." + "\n");
	            messageArea.append("******************************\n");
	            textField.setVisible(false);
	            out.println("/police");
	         } else if (line.startsWith("object_description")) {
	            line = line.substring(18);
	            System.out.println(line);
	            if (line.startsWith("room1,")) {
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            } else if (line.startsWith("room2,")) {
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            } else if (line.startsWith("foot size,")) {
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }

	            } else if (line.startsWith("mafia foot size,")) {
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            }
	            JOptionPane.showMessageDialog(actionFrame, line, "CLUE", JOptionPane.PLAIN_MESSAGE);
	         } else if (line.startsWith("FOUND")) {
	            String first = line.substring(5, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String last = line;
	            if (last.equals("everyone_select")) {
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append(first + "(이)가 단서를 발견했습니다." + "\n");
	            } else {
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append(first + "(이)가 단서를 발견했습니다." + "\n");
	               messageArea.append(last + "(이)가 수색할 차례입니다." + "\n");
	            }
	         } else if (line.startsWith("CLUEFINDER")) {
	            String first = line.substring(10, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String middle = line.substring(0, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String last = line;
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append(first + ", " + middle + ", " + last + "(이)가 선택 되었습니다.\n");
	            messageArea.append(first + "(이)가 수색할 차례입니다.\n");
	         } else if (line.startsWith("SHOW_JOB")) {
	            line = line.substring(8);
	            line = line.substring(0, line.indexOf(" ")) + "\n" + line.substring(line.indexOf(" ") + 1);

	            String[] divide = line.split("/");

	            for (int i = 0; i < divide.length; i++) {
	               if (i == 0)
	                  line = divide[i] + "\n";
	               else
	                  line += divide[i] + "\n";
	            }

	            JOptionPane.showMessageDialog(actionFrame, line, "Job", JOptionPane.PLAIN_MESSAGE);
	         }

	         else if (line.startsWith("SHOW_STORY")) {
	            line = line.substring(10);
	            String[] selections = line.split(",");
	            String total = "";
	            String[] divide = selections[0].split("/");

	            for (int i = 0; i < divide.length; i++) {
	               if (i == 0)
	                  selections[0] = divide[0];
	               else {
	                  if (i == divide.length - 1)
	                     selections[0] += divide[i];
	                  else
	                     selections[0] += divide[i] + "\n";
	               }

	            }
	            for (int i = 1; i < selections.length; i++) {
	               String[] divide_job = selections[i].split("/");
	               for (int j = 0; j < divide_job.length; j++) {
	                  if (j == 0)
	                     selections[i] = divide_job[j] + "\n";
	                  else
	                     selections[i] += divide_job[j] + "\n";
	               }
	            }

	            for (int i = 0; i < selections.length; i++) {
	               if (i % 2 == 0)
	                  total += selections[i] + "\n\n";
	               else
	                  total += selections[i] + "\n";
	            }
	            JOptionPane.showMessageDialog(actionFrame, total, "Story", JOptionPane.PLAIN_MESSAGE);
	         } else if (line.startsWith("KICKED")) {
	            line = line.substring(6);
	            String[] divide = line.split(",");
	            for (int i = 0; i < divide.length; i += 2) {
	               if (i == 0)
	                  line = divide[i] + "\n" + divide[i + 1].substring(0,divide[i + 1].indexOf(" ")) + "\n\n";
	               else
	                  line += divide[i] + "\n" + divide[i + 1].substring(0,divide[i + 1].indexOf(" ")) + "\n\n";
	            }
	            JOptionPane.showMessageDialog(actionFrame, line, "DEAD", JOptionPane.PLAIN_MESSAGE);
	            textField.setVisible(false);
	            is_kicked = true;
	         }
	         messageArea.setCaretPosition(messageArea.getDocument().getLength());
	      }

	   }

	   public String vote(String line) {
	      String candidate = null;
	      String[] selections = line.split(",");
	      for (int i = 0; i < selections.length; i++)
	         System.out.println(selections[i]);

	      candidate = (String) JOptionPane.showInputDialog(null, "누가 마피아일까...", "VOTE", JOptionPane.QUESTION_MESSAGE,
	            null, selections, "user1");
	      // null에는 이 팝업을 띄울 pane의 이름을 적는다.
	      return candidate; // ->candidate를 리턴함.
	   }

	   public String mafia(String line) {
	      String candidate = null;
	      String[] selections = line.split(",");
	      for (int i = 0; i < selections.length; i++)
	         System.out.println(selections[i]);

	      candidate = (String) JOptionPane.showInputDialog(null, "누구를 죽일까...", "MAFIA", JOptionPane.QUESTION_MESSAGE,
	            null, selections, "user1");
	      // null에는 이 팝업을 띄울 pane의 이름을 적는다.
	      return candidate; // ->candidate를 리턴함.
	   }

	   public String police(String line) {
	      String selected = null;
	      String[] selections = line.split(",");
	      for (int i = 0; i < selections.length; i++)
	         System.out.println(selections[i]);// 투표를 위해 유저이름을 담아놓음.

	      selected = (String) JOptionPane.showInputDialog(null, "누구의 직업을 수사할까....", "POLICE",
	            JOptionPane.QUESTION_MESSAGE, null, selections, "user1");
	      // null에는 이 팝업을 띄울 pane의 이름을 적는다.
	      return selected;
	   }

	   public String doctor(String line) {
	      String protect = null;
	      String[] selections = line.split(",");
	      for (int i = 0; i < selections.length; i++)
	         System.out.println(selections[i]);// 투표를 위해 유저이름을 담아놓음.

	      protect = (String) JOptionPane.showInputDialog(null, "누구를 지킬까....", "DOCTOR", JOptionPane.QUESTION_MESSAGE,
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
	      return;
	   }
	}