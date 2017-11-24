/*기존의 test_timer_func*/
// 서버에서  어느시점에넣어야할지아직미정
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Toolkit;

public class testYet_timer {
    
	public static void main(String[] args){
		Timer m_timer=new Timer();
		TimerTask m_task=new TimerTask(){
			public void run(){ //스케줄 설정한 시간 뒤에 실행 할 부분
				Toolkit.getDefaultToolkit().beep(); //알람, beep
				// vote();
			}
		};
		m_timer.schedule(m_task,5000); //5초로 임의 설정된 상태
	}
}
