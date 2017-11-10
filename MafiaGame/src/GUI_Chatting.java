/*client_채팅창과 투표패널GUI*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI_Chatting {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    public GUI_Chatting() {
        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
       
        frame.getContentPane().add(textField, "North"); //채팅창은 맨 윗쪽에 배치
        frame.getContentPane().add(new JScrollPane(messageArea), "Center"); //스크롤은 중앙에 배치
        frame.getContentPane().add(new JScrollPane(messageArea), "Center"); 
        frame.pack(); //전체 채팅창을 보여줌

        // Add Listeners
        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText()); //텍스트에서 입력받은 글 프린트 하기
                textField.setText("");
            }
        });
    }

/*어떤 서버에 접속할 것인지 입력받음*/
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

/*게임에서 사용할 이름을 입력받음*/
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

/*서버접속과 이름입력창을 실행*/
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
            //if(5분이 됐다고 서버에게 알림을 받으면)
            String candidate=null;
            String[] selections={"user1","user2","user3","user4","user5","user6","user7"};//투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.
            candidate=(String) JOptionPane.showInputDialog(null, "5분이 지났습니다. 누구를 정지시키겠습니까?", "vote", JOptionPane.QUESTION_MESSAGE,null,selections,"user1");
            //null에는 이 팝업을 띄울 pane의 이름을 적는다.
            //return candidate(); ->서버에게 candidate를 리턴함.
        }
       
     
    }

	public static void main(String[] args) throws Exception {
		GUI_Chatting client = new GUI_Chatting();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //나가기 버튼을 누르면 나감
        client.frame.setVisible(true); //채팅창을 보여줌
        client.run(); //서버접속, 이름입력 창을 띄움

	}

}
