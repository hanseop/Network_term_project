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

	public void setOffStopMe(boolean flag)

	{
		stopMe = flag;
	}

	public void run()

	{
		while (true)

		{

			myLabel.setText("" + y);
			try {

				Thread.sleep(1000);

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

						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (y == -1) {
				Send_socket.out.println("/timeout");
				this.interrupt();
				break;
				
			}
		}
		
	}
	public int getTime()
	{
		return y;
	}
}
/*
 * GUI for timer
 */
public class Timer_start extends JFrame implements ActionListener, Runnable {

	static JLabel timerLabel = null;
	JLabel secondLabel = null;
	JButton stopbutton = null;
	SecondThread secondThread = null;
	Object monitorObj = new Object();

	public void TimerStart()

	{
		this.setTitle("Timer Test");
		Container c = this.getContentPane();
		c.setLayout(new FlowLayout());
		secondLabel = new JLabel("0");
		secondLabel.setFont(new Font("Gothic", Font.ITALIC, 80));
		// stopbutton = new JButton("Stop");
		// stopbutton.addActionListener(this);

		c.add(secondLabel);
		// c.add(stopbutton);

		this.setSize(300, 150);
		this.setVisible(true);

		secondThread = new SecondThread(secondLabel,monitorObj);
		secondThread.start();
		
		while(!secondThread.interrupted()){
			if(secondThread.getTime() == -1)
				break;
		}
		this.setVisible(false);
	}

	public void run() {
		this.TimerStart();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton btn = (JButton) e.getSource();
		if (btn.getText().equals("Stop"))

		{
			stopbutton.setText("Resume");
			secondThread.setOffStopMe(true);
		} else {
			synchronized (monitorObj) {
				monitorObj.notify();
			}
			stopbutton.setText("Stop");
			secondThread.setOffStopMe(false);

		}
	}

}