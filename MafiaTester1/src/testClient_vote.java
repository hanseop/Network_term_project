/*기존의 GUI_chatting 클래스*/
/********************************************
 * vote담당 클라이언트가 해야할일
 * 
 * 1. 서버로부터 유저이름 받아오고, 패널안에 넣어놓기
 * 2. 서버에게 클라이언트가 투표한 후보자 이름을 보내주기
 *******************************************/

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

/*일단 투표패널팝업이 뜨는 지 확인하려고 pa의 채팅창 기능에 vote함수를 추가한 상태*/
// gui가 완성되면 합쳐야 함.

public class testClient_vote{

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    public testClient_vote() {
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
   // private void run(String[] players) throws IOException {
    // 서버로부터 유저 이름을 받아오면 위의 라인으로 실행해야됨. string값을 받아오는 상태.
    
    // 지금은 테스트를 위해서 아래 라인으로 실행함. string값을 받아오지 않는 가정하에 테스트.
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        int count=0;
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
            /* 원래 이게 맞는 코드!
             * if(서버가 5분이 되었다고 알려주면)
             *	out.println("vote"+vote(players)); //-> players는 유저이름이 담긴 스트링 배열
            */
           if(count==0){//테스트를 위해 돌아가는 부분
            out.println("vote "+vote());
           }
           count++;
            //System.out.println("vote "+vote());
        }
    }
    /* 원래 이게 맞는 코드!
     * public String vote (String[] users){
    	String candidate=null;
        String[] selections=users;//투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.
        candidate=(String) JOptionPane.showInputDialog(null, "5분이 지났습니다. 누구를 정지시키겠습니까?", "vote", JOptionPane.QUESTION_MESSAGE,null,selections,"user1");
        //null에는 이 팝업을 띄울 pane의 이름을 적는다.
        return candidate; //->서버에게 candidate를 리턴함.
    }*/
    
    public String vote (){ //테스트용 투표패널.
    	String candidate=null;
        String[] selections={"a","b","c"};//투표를 위해 유저이름을 담아놓음. 서버에서 받아와야 함.
        candidate=(String) JOptionPane.showInputDialog(null, "5분이 지났습니다. Whom do you want to kill?", "vote", JOptionPane.QUESTION_MESSAGE,null,selections,"user1");
        //null에는 이 팝업을 띄울 pane의 이름을 적는다.
        return candidate; //->서버에게 candidate를 리턴함.
    } 

	public static void main(String[] args) throws Exception {
		testClient_vote client = new testClient_vote();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //나가기 버튼을 누르면 나감
        client.frame.setVisible(true); //채팅창을 보여줌
        //client.run(players); //서버접속, 이름입력 창을 띄움, 이게 맞는 코드!!
        
        client.run(); //테스트용
	}

}
