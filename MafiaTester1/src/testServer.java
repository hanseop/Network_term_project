package net_hw2;

/*서버 코드 주석을 일단 적어 본 상태.
 * 서버가 할 일
 * 1. 현재 코드 정리하기, 함수로구성하면좋을것같아요
 * 2. 꼭! 유저 이름이 담긴 set을 클라이언트에게 보내주어야함.
 *      그래야만 투표할때 패널에서 후보를 고를 수 있게할 수 있음.
 * 3. 클라이언트들이 투표에서 뽑은 후보를 넘겨받으면, kill할 사람을 찾고, 관전 시킬지 아웃 시킬지 코드 짜줘야됨.
 * 4. 투표를 언제 시행할건지 5분 타이머를 잴 것인데, 타이머를 언제 시작할지 넣어줘야됨.
 *      타이머를 짜놓은 함수는 깃허브에 testYet_timer라고 업로드해두었음.   
 * */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

/**********************************************************************************************************
 * class CharServer
 * 
 * Wait until the 7 players gather. After gathering, assign a job to all
 * players. Alert the starting of game. After 5 minutes from starting, tell
 * players(clients) to vote to kill.
 *
 * @ PORT[int] : port number. @ names[String//HashSet] : hashSet about name of
 * players. @ writers[PrintWriter//HashSet] : hashset about
 * writer????????????????????? 어떤 기능을 하는 변수인지.... -> 유저의 주소를 받는 HashSet(중복 허용
 * x) @ info[str,pw//HashMap] : hashmap about set including name and writer. @
 * max_client[int] : the maximum number of player, 7. @ client_count[int] :
 * number for counting players. @ vote[int] : array for storing number after
 * voting. @ user[string] : array of user. @ ID[printWriter] : array of user's
 * ID. @ job[string] : array of job for players. @ random[int] : for allocating
 * jobs to players, initial the information in array.
 **********************************************************************************************************/

public class testServer {

	private static final int PORT = 9001;
	private static HashSet<String> names = new HashSet<String>();
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>();
	private static int max_client = 7;
	private static int client_count = 0;

	/**************************** modified *******************************/
	private static int current_client = max_client;
	private static int is_vote = 0;
	private static int mafia_index = 9999;
	private static int police_index = 9999;
	private static int doctor_index = 9999;
	private static int[] kicked = new int[max_client];
	private static int[] vote = new int[max_client];
	/**************************************************************/

	private static String[] user = new String[max_client];
	private static PrintWriter[] ID = new PrintWriter[max_client];
	private static String[] job = { "citizen i'm citizen", "doctor i'm doctor", "citizen i'm citizen",
			"mafia i'm mafia", "citizen i'm citizen", "police i'm police", "citizen i'm citizen" };

	private static int[] random = { -1, -1, -1, -1, -1, -1, -1 };

	/* initialize the informations of vote, user and ID */
	private static void initialize(int[] vote, int[] kicked, String[] user, PrintWriter[] ID) {
		for (int i = 0; i < max_client; i++) {
			/**************************** modified *******************************/
			kicked[i] = 1;/**************************************************************/
			vote[i] = 0;
			user[i] = "null";
			ID[i] = null;
		}
	}

	/* assign a job to players randomly */
	private static void randomArray(int[] random) {
		int index = 0;
		while (true) {
			int value = (int) (Math.random() * 7);
			int cnt = 0;
			for (int i = 0; i < random.length; i++) {
				if (value == random[i])
					cnt++;
			}
			if (cnt == 0) {
				random[index] = value;
				index++;
			}
			if (index == random.length)
				break;
		}
	}

	public static void storeIndex() {
		for (int i = 0; i < max_client; i++) {
			if ((job[i].substring(0, job[i].indexOf(" "))).equals("mafia"))
				mafia_index = i;
			else if ((job[i].substring(0, job[i].indexOf(" "))).equals("police"))
				police_index = i;
			else if ((job[i].substring(0, job[i].indexOf(" "))).equals("doctor"))
				doctor_index = i;
		}
	}

	public static void whisper(String input, String name) {
		String whisper;
		whisper = input.substring(1, input.indexOf("/>"));

		if (names.contains(whisper)) {
			PrintWriter sender = info.get(name);
			PrintWriter receiver = info.get(whisper);
			receiver.println("MESSAGE " + "<whisper from " + name + "> : " + input.substring(whisper.length() + 3));
			sender.println("MESSAGE " + "<whisper to " + whisper + "> : " + input.substring(whisper.length() + 3));
		} else {
			PrintWriter sender = info.get(name);
			sender.println("MESSAGE " + "This user does not exist.");
		}

	}

	public static void whatIsMyJob(String name) {
		int temp_index = 0;

		for (int i = 0; i < client_count; i++) {
			if (name == user[i])
				temp_index = i;
		}

		PrintWriter sender = info.get(name);
		sender.println("MESSAGE " + "your job is " + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
	}

	public static void whatIsMyRole(String name) {
		int temp_index = 0;
		for (int i = 0; i < client_count; i++) {
			if (name == user[i])
				temp_index = i;
		}
		PrintWriter sender = info.get(name);
		sender.println("MESSAGE " + "your role is " + job[temp_index].substring(job[temp_index].indexOf(" ")));
	}

	public static void vote() {
		String temp = null;
		for (int i = 0; i < max_client; i++) {
			if (kicked[i] != 0) {
				if (temp == null) {
					temp = user[i];
				} else {
					temp += ("," + user[i]);
				}
			}
		}
		System.out.println(temp);
		for (PrintWriter writer : writers) {
			writer.println("VOTENAME " + temp);
		}
	}

	public static void whoIsVictim(String input) {
		String victim = input.substring(7);
		int temp_index = 0;
		for (int i = 0; i < max_client; i++) {
			if (user[i].equals(victim) && kicked[i] != 0)
				temp_index = i;
		}
		is_vote++;
		vote[temp_index]++;
	}

	public static void showJob(String name) {
		PrintWriter police = info.get(name);
		String temp = null;
		for (int i = 0; i < max_client; i++) {
			if (kicked[i] != 0 && !user[i].equals(user[police_index])) {
				if (temp == null) {
					temp = user[i];
				} else {
					temp += ("," + user[i]);
				}
			}
		}
		System.out.println(temp);
		if (name.equals(user[police_index])) {
			police.println("JOB" + temp);
		} else {
			police.println("MESSAGE " + "You are not police");
		}
	}

	public static void isHeMafia(String name, String input) {
		PrintWriter police = info.get(name);
		String selected = input.substring(8);
		int temp_index = 0;
		for (int i = 0; i < max_client; i++) {
			if (user[i].equals(selected) && kicked[i] != 0)
				temp_index = i;
		}
		police.println("IS_MAFIA?" + user[temp_index] + "' job is"
				+ job[temp_index].substring(job[temp_index].indexOf(" ") + 1));
	}

	public static void broadcast(String name, String input) {
		for (PrintWriter writer : writers) {
			writer.println("MESSAGE " + name + ": " + input);
		}
	}

	public static void voteResult() {
		if (is_vote == client_count) {
			int count = 0;
			int temp_index = 0;
			int same = 0;
			for (int i = 0; i < max_client; i++) {
				if (vote[i] > count) {
					count = vote[i];
					temp_index = i;
				}
			}
			for (int i = 0; i < max_client; i++) {
				if (count == vote[i] && i != temp_index)
					same = 1;
			}

			if (count == 0)
				same = 0;

			if (same != 1) {
				for (PrintWriter writer : writers) {
					writer.println("MESSAGE " + user[temp_index] + " kicked");
				}
				PrintWriter victim = info.get(user[temp_index]);
				victim.println("KICKED");
				kicked[temp_index] = 0;
				current_client--;
			} else {
				for (PrintWriter writer : writers) {
					writer.println("MESSAGE " + "Nothing happened");
				}
			}
			is_vote = 0;
			count = 0;
			for (int i = 0; i < max_client; i++)
				vote[i] = 0;
		}
	}

	public static void whoIsWin() {
		if (kicked[mafia_index] == 0) {
			for (PrintWriter writer : writers) {
				writer.println("MESSAGE " + "mafia dead!, citizen win!");
			}
			System.exit(0);
		} else if (current_client == 2) {
			for (PrintWriter writer : writers) {
				writer.println("MESSAGE " + "mafia win!");
			}
			System.exit(0);
		}
	}

	/* assign a job and intialize information. Then, go to handler. */
	public static void main(String[] args) throws Exception {

		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT); // error?????????????????

		randomArray(random); // assign a job
		initialize(vote, kicked, user, ID); // intialize informations of each players

		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	/*************************************************************
	 * class Handler
	 * 
	 * this is for socket programming between server and clients.
	 * 
	 * @ name[String] : name @ socket[socket] : socket @ in[BufferedReader] :
	 * BufferedReader @ out[PrintWriter] : printwriter
	 *************************************************************/
	private static class Handler extends Thread {

		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		/* run for socket programming */
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // read from user
				out = new PrintWriter(socket.getOutputStream(), true); // print out to console

				while (true) {// read a name of player
					out.println("SUBMITNAME");
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (names) {
						// if any user enters, alert to every players.
						sendToallclient("CONNECT " + name + " is connected.\n");

						if (!names.contains(name)) { // there must be no duplicate in name.
							names.add(name);

							for (PrintWriter writer : writers) { // if any user enters, memo in server's console.
																	// 맞나..??? -> 새로운 유저가 들어올 경우 채팅창으로 유저들에게 [name]
																	// enter라고 알려줌
								writer.println("MESSAGE " + "[" + name + "] enter");
							}
							break;
						}
					}
				}
				out.println("NAMEACCEPTED"); // if user enters own name, give check-message. ??? ->클라이언트에게
												// NAMEACCPETED라는 프로토콜메세지를 보내줌 ->유저의 채팅창 활성화 시켜줌
				writers.add(out); // ??? -> 유저의 주소를 hashset에 저장
				user[client_count] = name; // store a name in array.
				ID[client_count] = out; // store an ID in array.

				client_count++; // for next users, do count++.

				System.out.println(user[client_count - 1] + "님이 입장하셨습니다.");
				System.out.println("현재 인원 " + client_count + "명");

				info.put(name, out);

				if (client_count == max_client) {
					// if all player gathers, start the game.
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + "game start");
					}

					// assign a job to player. ..? 맞는건가..??
					for (int i = 0; i < max_client; i++) {
						String temp = job[i];
						job[i] = job[random[i]];
						job[random[i]] = temp;
					}

					storeIndex();
					// tell a job to each players.? 이 과정이 맞나..?--직업배정받은 정보를 소켓에 담아 클라이언트에게 주면 팝업창으로
					// 알려주는 게 좋을 것 같음...
					for (int i = 0; i < max_client; i++)
						System.out.println(job[i]);
				}

				while (true) {
					// 여기서부터 어떤 과정인지 잘 모르겠음...
					String input = in.readLine();

					if (input == null) { // if input is null,
						return;
					}

					// whisper..?????? 필요한 이유..??? -> PA할때 쓰던 코드를 그대로 써서 있음 삭제해도 무방
					else if (input.startsWith("<") && input.indexOf("/>") != -1) {
						whisper(input, name);
					}
					// tell a job --명령어를 써야만 직업을 알려주는..??것 보다는 일방적으로 알려주는게 좋을 것 같아요~
					// 직업배정받은 정보를 소켓에 담아 클라이언트에게 주면 팝업창으로 알려주는 게 좋을 것 같음... !
					// 현재 서버에 구현해놓은 기능은 gui와 연동되지 않고 순수 기능만 구현해 놓았습니다. 추후 gui가 완성되면 구현 예정
					else if (input.startsWith("/") && input.indexOf("job") != -1) {
						whatIsMyJob(input);
					}
					// job과 role의 차이??? job : 직업 role : 역할(능력)
					else if (input.startsWith("/") && input.indexOf("role") != -1) {
						whatIsMyRole(input);
					}
					// 투표는 5분타이머 이후에 패널-팝업창을 띄워서 바로 하게 만드는게 좋을 것 같아요.
					// 명령어를 입력하고 투표하는 방식은, 클라이언트마다 각자 다른 시기에 선택할 것 같아서요~
					// 현재 서버에 구현해놓은 기능은 gui와 연동되지 않고 순수 기능만 구현해 놓았습니다. 추후 gui가 완성되면 구현 예정

					// 이름??? 뭐하는 부분인지 잘 모르겠어요...
					// 11월24일 모임때 말씀하셨던 투표를 할때 서버에서 클라이언트들의 이름을 받아오기 위한 과정입니다. 유저가 채팅창에 /name이라고 치면
					// 현재 들어와 있는 유저들의 이름이 gui팝업창으로 뜸
					// 아직 미완성
					/***************************************
					 * modified
					 *******************************************/
					else if (input.startsWith("/") && input.indexOf("name") != -1) {
						vote();
					} else if (input.startsWith("/") && input.indexOf("victim") != -1) {
						whoIsVictim(input);
					} else if (input.startsWith("/") && input.indexOf("police") != -1) {
						showJob(name);
					} else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
						isHeMafia(input, input);
					}
					// 뭐하는 부분인지 잘 모르겠어요..ㅠㅠ
					// 유저가 채팅을 치면 다른 유저들에게 보내주는 기능
					else {
						broadcast(input, input);
					}
					voteResult();
					whoIsWin();

					/*****************************************************************************/
				}
			} catch (IOException e) { // 갑자기 catch가 나온 이유..? -> 에러 발생시
				System.out.println(e);
			} finally { // if client is out, alert.
				if (name != null) {
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + "[" + name + "] exit");
					}
					names.remove(name);
					info.remove(name);
					client_count--;
					System.out.println("한명 나갔다 " + client_count);
				}
				if (out != null) {
					writers.remove(out);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}

	}

	// 어떤것을 클라이언트에게 알려주는 것인지 잘 모르겠어요~
	public static void sendToallclient(String mssg) {
		for (PrintWriter writer : writers) {
			writer.println(mssg);
			writer.flush();
		}
	}
}