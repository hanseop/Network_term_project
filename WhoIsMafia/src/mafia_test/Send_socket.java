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
/*
 * Class send socket send/receive protocol with server.
 * It first gets Server IP address and user nickname and wait for
 * 7 users to come to mafia game.
 * There are various protocol ->protocol for mafia, police, doctor, citizens ...
 */
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
	/*
	 * initalize message area and textfield
	 */
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
	/*
	 * get server's address
	 */
	 private String getServerAddress() {
	      return JOptionPane.showInputDialog(frame, "서버의 IP주소를 입력해주세요:", "Who is the mafia", JOptionPane.PLAIN_MESSAGE);
	   }

	   /* get users nickname */
	   private String getsName() {
	      return JOptionPane.showInputDialog(frame, "게임에서 사용할 닉네임을 입력해주세요:", "Who is the mafia",
	            JOptionPane.PLAIN_MESSAGE);
	   }
	   /*
	    * runChat method send and gets reply with server with various protocol
	    */
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
	         if (line.startsWith("SUBMITNAME")) { // get user's nickname
	            out.println(getsName());
	         } else if (line.startsWith("NAMEACCEPTED")) { // if server accept the username
	            textField.setEditable(true);
	         } else if (line.startsWith("MESSAGE")) { // if client get message protocol the message is for chatting
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
	         // if server tells that 5minutes has elapsed

	         
	         else if (line.startsWith("JOB")) { // when police turn -> when job protocol is received police can know user's job
	            line = line.substring(3);
	            String selected = police(line);
	            out.println("/is_he_mafia?" + selected); // Send server is_MAFIA protocol to know user's job
	         } else if (line.startsWith("IS_MAFIA?")) { // server tells user's role to police
	            messageArea.append(line.substring(9) + "\n");
	            out.println("/kill");
	         } else if (line.startsWith("NON")) { // when police is dead
	            out.println("/kill");
	         } else if (line.startsWith("VOTENAME ")) { // When server indicates to user to vote
	            if (is_kicked == false) {
	               line = line.substring(9);
	               String victim = vote(line);
	               out.println("/victim" + victim); // send the selected user
	            }
	         } else if (line.startsWith("KILL")) { // the user that mafia wants to kill
	            line = line.substring(4);
	            String victim = mafia(line);
	            out.println("/dead" + victim);
	         } else if (line.startsWith("DEAD")) { // broadcast dead user
	            messageArea.append(line.substring(4) + "\n");
	         } else if (line.startsWith("DOCTOR")) { // server tells doctor to choose user to save
	            line = line.substring(6);
	            String protect = doctor(line);
	            out.println("/protect" + protect); // send the user doctor wants to save 
	         } else if (line.startsWith("D_START")) { // server tells user that day has started
	            for (int i = 0; i < RoomGUI.index; i++) // when night comes make user cannot click objects 
	               RoomGUI.button[i].setEnabled(true);
	            if (is_kicked == false) // chatting area only available for alive users
	               textField.setVisible(true);
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append("아침이 밝았습니다." + "\n");
	            messageArea.append("******************************\n");
	            messageArea.append(line.substring(7) + "\n");
	         } else if (line.startsWith("T_START")) { // server tells users to start timer
	            for (int i = 0; i < RoomGUI.index; i++)
	               RoomGUI.button[i].setEnabled(false);
	            if (line.indexOf("all object selected") != -1) { // case when all objects are selected
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append("충분한 조사가 이루어졌습니다." + "\n");
	               messageArea.append("밤이 되기 전에 사람들과 의논하여 마피아를 추려내세요." + "\n");
	               messageArea.append("******************************\n");
	            }
	            Thread t3 = new Thread(new Timer_start());
	            t3.start();
	         } else if (line.startsWith("V_END")) { // server tells that vote is end
	            messageArea.append(line.substring(5) + "\n\n");
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append("한밤중이 되었습니다." + "\n");
	            messageArea.append("사람들은 방에 들어가 각자의 일을 하거나 취침을 합니다." + "\n");
	            messageArea.append("******************************\n");
	            textField.setVisible(false);
	            out.println("/police");
	         } else if (line.startsWith("object_description")) { // server tells user the information about object 
	            line = line.substring(18);
	            if (line.startsWith("room1,")) { // information about room1
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            } else if (line.startsWith("room2,")) { // information about room2
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            } else if (line.startsWith("foot size,")) { // information about footsize
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }

	            } else if (line.startsWith("mafia foot size,")) { // information about mafia foot size
	               String[] divide = line.split(",");
	               line = "";
	               for (int i = 0; i < divide.length; i++) {
	                  line += divide[i] + "\n";
	               }
	            }
	            JOptionPane.showMessageDialog(actionFrame, line, "CLUE", JOptionPane.PLAIN_MESSAGE);
	         } else if (line.startsWith("FOUND")) { // case when user found usful information tells next user to choose object
	            String first = line.substring(5, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String last = line;
	            if (last.equals("everyone_select")) { // case when every users selected object
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append(first + "(이)가 단서를 발견했습니다." + "\n");
	            } else {
	               messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	               messageArea.append(first + "(이)가 단서를 발견했습니다." + "\n");
	               messageArea.append(last + "(이)가 수색할 차례입니다." + "\n");
	            }
	         } else if (line.startsWith("CLUEFINDER")) { // server tells which user to select object
	            String first = line.substring(10, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String middle = line.substring(0, line.indexOf(","));
	            line = line.substring(line.indexOf(",") + 1);
	            String last = line;
	            messageArea.append("*******[SYSTEM MESSAGE]*******\n");
	            messageArea.append(first + ", " + middle + ", " + last + "(이)가 선택 되었습니다.\n");
	            messageArea.append(first + "(이)가 수색할 차례입니다.\n");
	         } else if (line.startsWith("SHOW_JOB")) { // case when user clicked job button
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

	         else if (line.startsWith("SHOW_STORY")) { // case when user clicked story button
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
	            JOptionPane.showMessageDialog(actionFrame, total, "Story", JOptionPane.PLAIN_MESSAGE); // pop up GUI frame
	         } else if (line.startsWith("KICKED")) { // case when user is kicked (mafia murder or vote...)
	            line = line.substring(6);
	            String[] divide = line.split(","); // tell kicked user about other user's information (nickname, job ...)
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
	         messageArea.setCaretPosition(messageArea.getDocument().getLength()); // scroll down to current chatting
	      }

	   }

	   public String vote(String line) {
	      String candidate = null;
	      String[] selections = line.split(",");

	      candidate = (String) JOptionPane.showInputDialog(null, "누가 마피아일까...", "VOTE", JOptionPane.QUESTION_MESSAGE,
	            null, selections, "user1");
	      // null에는 이 팝업을 띄울 pane의 이름을 적는다.
	      return candidate; // ->candidate를 리턴함.
	   }

	   public String mafia(String line) {
	      String candidate = null;
	      String[] selections = line.split(",");

	      candidate = (String) JOptionPane.showInputDialog(null, "누구를 죽일까...", "MAFIA", JOptionPane.QUESTION_MESSAGE,
	            null, selections, "user1");
	     
	      return candidate; 
	   }

	   public String police(String line) {
	      String selected = null;
	      String[] selections = line.split(",");

	      selected = (String) JOptionPane.showInputDialog(null, "누구의 직업을 수사할까....", "POLICE",
	            JOptionPane.QUESTION_MESSAGE, null, selections, "user1");
	      
	      return selected;
	   }

	   public String doctor(String line) {
	      String protect = null;
	      String[] selections = line.split(",");

	      protect = (String) JOptionPane.showInputDialog(null, "누구를 지킬까....", "DOCTOR", JOptionPane.QUESTION_MESSAGE,
	            null, selections, "user1");
	      
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