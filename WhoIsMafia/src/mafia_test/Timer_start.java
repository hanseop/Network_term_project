package net_hw2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
/**************************************************************
 * Class timer indicates user that they have 5minutes to chat each other
 * timer class tells time to user with a seperate frame with JLabel
 * if the timer is finished it sends protocol to server that timeout happened
 **************************************************************/
class SecondThread extends Thread

{
	int y = 90;
	JLabel myLabel = null;
	private boolean stopMe = false;
	private Object monitorObj = null;

	public SecondThread(JLabel myLabel, Object _monitorObj) {

		this.myLabel = myLabel;
		monitorObj = _monitorObj;
	}
	/*
	 * method run decrease y value 1 after 1 second elapsed
	 */
	public void run() 

	{
		while (true)

		{

			myLabel.setText("" + y); // add y value to label to show how much time left for discussion
			try {

				Thread.sleep(1000); // make thread sleep till 1 second

			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			if (stopMe == false) {
				y--;
			} else {
				synchronized (monitorObj) {
					try {
						monitorObj.wait();

					} catch (InterruptedException e) {

						
						e.printStackTrace();
					}
				}
			}
			if (y == -1) { // if timer becomes 0 second client socket send to server timeout protocol
				Send_socket.out.println("/timeout"); // sending message to server
				this.interrupt(); //interrupt thread
				break;
				
			}
		}
		
	}
}
/*
 * GUI for timer
 */
public class Timer_start extends JFrame implements Runnable {

	static JLabel timerLabel = null;
	JLabel secondLabel = null;
	JButton stopbutton = null;
	SecondThread secondThread = null;
	Object monitorObj = new Object();

	public void TimerStart()

	{
		this.setTitle("Timer Test"); // title for timer frame
		
		
		
		Container c = this.getContentPane(); // frame for timer
		c.setLayout(new FlowLayout());
		secondLabel = new JLabel("0"); // timer label
		secondLabel.setFont(new Font("Gothic", Font.ITALIC, 80));
		c.add(secondLabel);
		this.setSize(300, 150);
		this.setVisible(true);
		
		secondThread = new SecondThread(secondLabel,monitorObj);
		secondThread.start();
		while(!secondThread.interrupted()){ // interrupt timer thread when the time becomes 0 and close timer Frame
			if(secondThread.getTime() == -1)
				break;
		}
		this.setVisible(false);
	}

	public void run() {
		this.TimerStart(); // thread for timer
	}

}