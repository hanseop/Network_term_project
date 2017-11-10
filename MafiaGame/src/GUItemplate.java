//seokbin's client
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class GUItemplate {
   JFrame frame = new JFrame("objectMessage");
   JFrame myMainWindow = new JFrame("Who is Mafia?");
   
   ImageIcon image2 = new ImageIcon("room2.jpg");
   Image newImage = image2.getImage();
   Image changedImage = newImage.getScaledInstance(800,500,Image.SCALE_SMOOTH);
   ImageIcon image1 = new ImageIcon(changedImage);
   
   ImageIcon image4 = new ImageIcon("object.png");
   Image newImage1 = image4.getImage();
   Image changedImage2 = newImage1.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
   ImageIcon image3 = new ImageIcon(changedImage2);
   
   ImageIcon key = new ImageIcon("key.png");
   Image newImage2 = key.getImage();
   //Image changedImage3 = newImage2.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
   
   JButton jB = new JButton(image3);
     JButton key1 = new JButton(key);
     
   JPanel  firstPanel = new JPanel(){
       public void paintComponent(Graphics g){
          jB.setBounds(500,120,50,50);
           key1.setBounds(600,400,50,30);
          g.drawImage(image1.getImage(),0,0,null);
          
          }
    };
    
     
    private void runGUI() {
       
        myMainWindow.setBounds(10, 10, 1000, 1000);
        myMainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myMainWindow.setLayout(new GridLayout(1,1));
        createFirstPanel();
        myMainWindow.getContentPane().add(firstPanel);
        
        myMainWindow.setVisible(true);
    }

    private void createFirstPanel() {
       
      
        firstPanel.setLayout(new FlowLayout());
        jB.setBackground(Color.red); 
        jB.setBorderPainted(false); 
        jB.setFocusPainted(false); 
        jB.setContentAreaFilled(false);
        key1.setBackground(Color.red); 
        key1.setBorderPainted(false); 
        key1.setFocusPainted(false); 
        key1.setContentAreaFilled(false);
        firstPanel.add(jB);
        firstPanel.add(key1);
        
       
        jB.addActionListener(new TheHandler());
        key1.addActionListener(new TheHandler());
       }

    private class TheHandler implements ActionListener { 
        public void actionPerformed(ActionEvent event) {
           JOptionPane.showMessageDialog(
                       frame,
                       "Message",
                       "Object Message",
                       1);
    }
        }
    

    public static void main(String[] args) {
        GUItemplate gt = new GUItemplate();
        gt.runGUI();
    }
}
