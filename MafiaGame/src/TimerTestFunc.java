import java.util.Timer;
import java.util.TimerTask;

public class TimerTestFunc {
    
	public static void main(String[] args){
		Timer m_timer=new Timer();
		TimerTask m_task=new TimerTask(){
			public void run(){ //스케줄 설정한 시간 뒤에 실행 할 부분
				System.out.println("Morph");
			}
		};
		m_timer.schedule(m_task,5000); //5초로 임의 설정된 상태
	}
}