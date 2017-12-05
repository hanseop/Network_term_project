import sun.audio.*;
import java.io.*;

public class BGM {

	private static InputStream in;
	private static AudioStream _bgm;
	private static AudioPlayer mgp = AudioPlayer.player;
	
	public BGM(String bgm_name) {
		in = BGM.class.getResourceAsStream(bgm_name);
	}
	@SuppressWarnings("restriction")
	public void Play(){
		try {
			_bgm = new AudioStream(in);
			mgp.start(_bgm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void Stop(){
		mgp.stop(_bgm);
	}

}
