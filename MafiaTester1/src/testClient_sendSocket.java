
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

public class testClient_sendSocket implements Runnable {
   BufferedReader in;
   PrintWriter out;
   JFrame frame = new JFrame();
   JPanel panel = new JPanel();
   JTextField textField = new JTextField(20);
   JTextArea messageArea = new JTextArea(4,40);
   public testClient_sendSocket(){
      messageArea.setEditable(false);
      textField.setEditable(false);
      testClient_room.frame.getContentPane().add(textField,"South");
      testClient_room.frame.getContentPane().add(new JScrollPane(messageArea),"East");
      testClient_room.frame.setVisible(true);
      textField.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
   }
   
     private String getServerAddress(){
         return JOptionPane.showInputDialog(
             frame,
             "Enter IP Address of the Server:",
             "Who is the mafia",
             JOptionPane.PLAIN_MESSAGE);
     }
     
    /*게임에서 사용할 이름을 입력받음*/
     private String getsName(){
         return JOptionPane.showInputDialog(
             frame,
             "Choose a User's nikname:",
             "Who is the mafia",
             JOptionPane.PLAIN_MESSAGE);
     }
    
     /*아래 run 함수의 int page는 메인화면에서 입장할 때 만 유저의 닉네임을 받고 싶어 만든 변수입니다.*/
     void runChat(String[] players, int page) throws IOException {
         // Make connection and initialize streams
        String serverAddress = new String(getServerAddress());
        Socket socket = new Socket(serverAddress, 9001);
         in = new BufferedReader(new InputStreamReader(
             socket.getInputStream()));
         out = new PrintWriter(socket.getOutputStream(), true);
         /*아래 while문은 게임 내 프로토콜에서 KICKED되지 않으면 GUI가 영원히 종료 안 되는 문제가 있습니다..*/
        while (true) {
         String line = in.readLine();
         /*서버 주소를 두번 치고 들어가야 하는 문제가 있습니다..., 로컬에서는 GUI창을 여러개 띄워도 프로세스가 단 하나만 실행됩니다.*/
         if (line.startsWith("SUBMITNAME")) {
            out.println(getsName());
            
         } else if (line.startsWith("NAMEACCEPTED")) {
            textField.setEditable(true);
         } else if (line.startsWith("MESSAGE")) {
            messageArea.append(line.substring(8) + "\n");// input message
         } else if (line.startsWith("KICKED")) {
            textField.setEditable(false);
            break;
         }
      }
     }
     public void run(){
        try{
           this.runChat(null, 1);
        }catch(IOException e){
           e.printStackTrace();
        }
     }
}
