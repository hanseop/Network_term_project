package mafia_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class Chat_Server {
	private static final int PORT = 9001; // 포트번호
	private static HashSet<String> names = new HashSet<String>(); // 이름을 저장하는 hashset -> 중복 방지
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); // 클라이언트 주소를 저장하는 hashset -> 중복 방지
	private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>(); // 이름과 클라이언트 주소를 매핑해주는
																							// hashmap
	private static int max_client = 7; // 최대 클라이언트 수
	private static int max_object = 10; // 오브젝트의 수(단서)
	private static int client_count = 0; // 현재 들어온 유저들의 수

	private static int timer_flag = 0; // timeout이 온 클라이언트들의 수
	private static int clickedNum = 0; // 한 턴에 오브젝트를 누를 수있는 유저들의 수
	private static int current_client = max_client; // 현재 유저들의 수 // client_count와 다른 점 : client_count는 유저들이 들어올 때
	// count되어서 max_client과 같을때 게임이 시작된다. 하지만 current client는 kick이 되지않은 유저들의 수를
	// 나타낸다

	private static int is_vote = 0; // 투표를 한 유저들의 수 current_client 와 같아지면 처형함
	private static int mafia_index = 0; // 마피아의 인덱스
	private static int police_index = 0; // 경찰의 인덱스
	private static int doctor_index = 0; // 의사의 인덱스
	private static int victim_index = 0; // 마피아가 죽이려고 하는 희생자의 인덱스
	private static int[] temp = new int[] { -1, -1, -1 }; // room1과 room2에 user의 이름들을 넣을때, 인덱스가 곂치지 않게 해주는 배열

	private static boolean[] selectedByMafia = new boolean[max_client]; // 마피아에게 선택되었는지 안되어ㅓㅆ는지를 표시함 false면 선택되지 않은것,
																		// true면 선택된것
	private static boolean[] kicked = new boolean[max_client]; // 강퇴 되었는지 안되었는지 표시 false면 강퇴되지않은 것, true면 강퇴

	private static int[] vote = new int[max_client]; // 유저별로 투표 당한수?

	private static String[] user = new String[max_client]; // 유저 이름 저장 -> 왜 위에 hashset이 있는데 또 쓰느냐? -> 인덱스로 매핑하기 위해
	private static PrintWriter[] ID = new PrintWriter[max_client]; // 유저 주소 저장 -> 왜 위에 hashset이 있는데 또 쓰느냐? -> 인덱스로 매핑하기
																	// 위해
	private static String story = "Eight people went to the villa for vacation./"
			+ "Then two of them fought and one person died./"
			+ "You have to find the clue and find the culprit by voting."; // 스토리 저장
	private static String totalJob = ""; // 모든 직업들을 알려줌 -> story와 같이 나옴(버튼)
	private static String[] job = { "시민 시민입니다", "의사 의사입니다", "시민 시민입니다", "마피아 마피아입니다", "시민 시민입니다", "경찰 경찰입니다",
			"시민 시민입니다" }; // 직업 저장 배열

	private static int[] random = { -1, -1, -1, -1, -1, -1, -1 }; // index를 랜덤화 하기 위해서 random 어레이를 만들었음 -> 이또한 인덱스로 매핑

	private static int[] footSize = { 245, 250, 255, 260, 265, 270, 275 };
	private static int selectNum = 3; // 오브젝트를 선택할수있는 유저들의 수
	private static int objectCount = 0; // 오브젝트를 선택한 수 (전체)
	private static int[] canSelect = new int[selectNum]; // 유저별로 오브젝트를 선택할수있느니 없는지 표시
	private static boolean[] isClicked = new boolean[max_client]; // 유저가 오브젝트를 클릭했는지 클릭하지 않았는지 표시
	private static boolean[] object_flag = new boolean[max_object]; // 해당 오브젝트가 선택되었는지 안되었는지 저장
	private static String[] object_msg = { "경찰에 대한 정보", "의사에 대한 정보", "발자국이 방 1로 이어져 있다.", "마피아 방에 대한 정보",
			"마피아가 없는 방에 대한 정보", "전체 발사이즈", "마피아 발사이즈", "오늘 야식으로 라면이 나온다고 한다", "텀프로젝트가 너무 많다", "어제 져넉은 맛있었다." };

	/*
	 * 오브젝트를 선택할 수 있는 사람들을 고르는 함수
	 */
	private static void objectPerson() {
		int count = 0;
		while (true) {
			int client_value = (int) (Math.random() * max_client); // client_value에 0~6까지 랜덤 변수 지정
			int cnt = 0;

			/*
			 * canSelect 어레이 전체 값에서 client_value와 같은 값이 있는지 없는지 확인함, 같은 값이 있으면 cnt변수를 ++ 즉
			 * cnt 변수가 0이 아니라는 말은 중복되는 변수가 있다는 것
			 */
			for (int i = 0; i < selectNum; i++) {
				if (client_value == canSelect[i])
					cnt++;
			}

			/*
			 * cnt가 0이다 -> 중복되는 값이 없다. kicked[client_value]가 false인 경우 -> 오브젝트를 뽑을 사람의
			 * index를 client_value값으로 하는데, false라는 것은 해당 유저가 강퇴당하지 않았다는 것이다.
			 */
			if (cnt == 0 && kicked[client_value] == false) {
				canSelect[count] = client_value;
				count++;
			}

			/*
			 * 만약 3명의 사람이 오브젝트를 다 고른다면
			 */
			if (count == selectNum)
				break;
		}
	}

	/*
	 * 변수들을 초기화 함
	 */
	private static void initialize() {
		for (int i = 0; i < max_client; i++) {
			kicked[i] = false;
			selectedByMafia[i] = false;
			vote[i] = 0;
			isClicked[i] = false;
			user[i] = "null";
			ID[i] = null;
		}

		for (int i = 0; i < selectNum; i++) {
			canSelect[i] = 9999;
		}

		for (int i = 0; i < max_object; i++)
			object_flag[i] = true;

		/*
		 * story버튼을 누르면 나오는 정보
		 */
		totalJob = "," + job[0].substring(0, job[0].indexOf(" ")) + "," + job[0].substring(job[0].indexOf(" ") + 1)
				+ "," + job[1].substring(0, job[1].indexOf(" ")) + "," + job[1].substring(job[1].indexOf(" ") + 1) + ","
				+ job[3].substring(0, job[3].indexOf(" ")) + "," + job[3].substring(job[3].indexOf(" ") + 1) + ","
				+ job[5].substring(0, job[5].indexOf(" ")) + "," + job[5].substring(job[5].indexOf(" ") + 1);
	}

	/*
	 * random 어레이에 랜덤값을 중복되지않게 저장(0~6) ex : 1 5 3 6 2 4 0
	 */
	private static void randomArray() {
		int index = 0;
		while (true) {
			int client_value = (int) (Math.random() * max_client);
			int cnt = 0;
			for (int i = 0; i < max_client; i++) {
				if (client_value == random[i])
					cnt++;
			}
			if (cnt == 0) {
				random[index] = client_value;
				index++;
			}
			if (index == max_client)
				break;
		}
	}

	/*
	 * 주요 직업들의 인덱스 저장
	 */
	public static void storeIndex() {
		for (int i = 0; i < max_client; i++) {
			if ((job[i].substring(0, job[i].indexOf(" "))).equals("마피아"))
				mafia_index = i;
			else if ((job[i].substring(0, job[i].indexOf(" "))).equals("경찰"))
				police_index = i;
			else if ((job[i].substring(0, job[i].indexOf(" "))).equals("의사"))
				doctor_index = i;
		}
	}

	/*
	 * 경찰과 의사에 대한 오브젝트 메세지 수정
	 */
	private static void assignClue_pol_doc() {
		object_msg[0] = "소파 틈새에서 " + user[police_index] + "의 경찰 뱃지를 보았다.";
		object_msg[1] = "피아노 옆에병원 출입증이 있다. 아까 " + user[doctor_index] + "가 피아노를 칠 때 벗어 둔 것 같다.";
	}

	/*
	 * 마피아가 있는 방에 들어가 있던 사람들 명단
	 */
	private static void assignClue_room1() {
		object_msg[3] = "room1," + user[mafia_index];
		int index = 0;

		while (true) {
			int client_value = (int) (Math.random() * max_client);
			int cnt = 0;
			if (client_value != mafia_index) {
				for (int i = 0; i < 3; i++) {
					if ((client_value == temp[i]))
						cnt++;
				}
				if (cnt == 0) {
					temp[index] = client_value;
					object_msg[3] += "," + user[client_value];
					index++;
				}
				if (index == 3)
					break;
			}
		}
	}

	/*
	 * 마피아가 없는 방에 들어가 있던 사람들 명단
	 */
	private static void assignClue_room2() {
		object_msg[4] = "room2";
		int index = 0;
		int[] temp2 = new int[] { -1, -1, -1 };
		while (true) {
			int client_value = (int) (Math.random() * max_client);
			int cnt = 0;
			if (client_value != mafia_index) {
				for (int i = 0; i < 3; i++) {
					if ((client_value == temp[i] || client_value == temp2[i]))
						cnt++;
				}
				if (cnt == 0) {
					temp2[index] = client_value;
					object_msg[4] += "," + user[client_value];
					index++;
				}
				if (index == 3)
					break;
			}
		}
	}

	/*
	 * 전체 유저의 발사이즈 크기 정보
	 */
	private static void assignClue_totalFootSize() {
		object_msg[5] = "foot size," + user[0] + " : " + footSize[0] + "," + user[1] + " : " + footSize[1] + ","
				+ user[2] + " : " + footSize[2] + "," + user[3] + " : " + footSize[3] + "," + user[4] + " : "
				+ footSize[4] + "," + user[5] + " : " + footSize[5] + "," + user[6] + " : " + footSize[6];
	}

	/*
	 * 마피아의 발크기
	 */
	private static void assignClue_mafiaFootSize() {
		object_msg[6] = "mafia foot size," + (footSize[mafia_index] - 5) + " ~ " + (footSize[mafia_index] + 5);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT);

		randomArray();
		initialize();

		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	private static class Handler extends Thread {

		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				while (true) {
					out.println("SUBMITNAME");
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (names) {
						sendToallclient("CONNECT " + name + " is connected.\n");

						if (!names.contains(name)) {
							names.add(name);

							for (PrintWriter writer : writers) {
								writer.println("MESSAGE " + "[" + name + "] enter");
							}
							break;
						}
					}
				}
				out.println("NAMEACCEPTED");

				writers.add(out);
				user[client_count] = name;
				ID[client_count] = out;

				client_count++;

				System.out.println(user[client_count - 1] + "님이 입장하셨습니다.");
				System.out.println("현재 인원 " + client_count + "명");

				info.put(name, out);
				for (int i = 0; i < selectNum; i++)
					System.out.println(canSelect[i]);
				if (client_count == max_client) {
					objectPerson();
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + "game start");
					}
					for (PrintWriter writer : writers) {
						writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
								+ user[canSelect[2]]);
					}

					for (int i = 0; i < max_client; i++) {
						String temp = job[i];
						job[i] = job[random[i]];
						job[random[i]] = temp;
					}

					storeIndex();
					assignClue_pol_doc();
					assignClue_room1();
					assignClue_room2();
					assignClue_totalFootSize();
					assignClue_mafiaFootSize();
				}

				while (true) {
					if (kicked[mafia_index] == true) {
						for (PrintWriter writer : writers) {
							writer.println("MESSAGE " + "마피아가 죽었습니다. 시민들이 이겼습니다!");
						}
						System.exit(0);
					} else if (current_client == 2) {
						for (PrintWriter writer : writers) {
							writer.println("MESSAGE " + "시민과 마피아의 수가 같습니다. 마피아가 이겼습니다!");
						}
						System.exit(0);
					}
					String input = in.readLine();
					if (input == null) {
						return;
					}

					/*
					 * 유저가 job 버튼을 눌러서 client로부터 /job 프로토콜이 온 경우(RoomGUI와 연관 있음)
					 */
					else if (input.startsWith("/") && input.indexOf("job") != -1) {
						int temp_index = 0;

						for (int i = 0; i < client_count; i++) {
							if (name.equals(user[i]))
								temp_index = i;
						}

						PrintWriter sender = info.get(name);
						sender.println("SHOW_JOB" + job[temp_index]);
					}

					/*
					 * 유저가 story 버튼을 눌러서 client로부터 /story 프로토콜이 온 경우(RoomGUI와 연관 있음)
					 */
					else if (input.startsWith("/") && input.indexOf("story") != -1) {
						int temp_index = 0;

						for (int i = 0; i < client_count; i++) {
							if (name.equals(user[i]))
								temp_index = i;
						}

						PrintWriter sender = info.get(name);
						sender.println("SHOW_STORY" + story + totalJob);
					}

					/*
					 * 한 유저의 타이머가 끝난 경우
					 */
					else if (input.startsWith("/") && input.indexOf("timeout") != -1) {
						timer_flag++;
						System.out.println(timer_flag);
						/*
						 * 모든 유저의 타이머가 끝난 경우
						 */
						if (timer_flag == current_client) {
							String temp = null;
							for (int i = 0; i < max_client; i++) {
								if (kicked[i] == false) {
									if (temp == null) {
										temp = user[i];
									} else {
										temp += ("," + user[i]);
									}
								}
							}
							System.out.println(temp);
							/*
							 * 모든 유저들에게 VOTENAME이라는 프로토콜을 보냄 -> 유저들이 처형할 사람들의 리스트를 보내줌
							 */
							for (PrintWriter writer : writers) {
								writer.println("VOTENAME " + temp);
							}
							timer_flag = 0; // 다음 투표를 위해 0으로 초기화
						}
					}

					/*
					 * 유저들이 투표를 해서 받은 표를 유저별로 저장
					 */
					else if (input.startsWith("/") && input.indexOf("victim") != -1) {
						String victim = input.substring(7);
						int temp_index = 0;
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(victim) && kicked[i] == false)
								temp_index = i;
						}
						is_vote++;
						vote[temp_index]++;
					}

					/*
					 * 밤이 되어서 경찰의 역할을 실행해줌
					 */
					else if (input.startsWith("/") && input.indexOf("police") != -1) {
						PrintWriter police = info.get(name);
						String temp = null;

						/*
						 * 경찰을 제외한 유저들의 명단을 저장
						 */
						for (int i = 0; i < max_client; i++) {
							if (kicked[i] == false && !user[i].equals(user[police_index])) {
								if (temp == null) {
									temp = user[i];
								} else {
									temp += ("," + user[i]);
								}
							}
						}
						System.out.println(temp);

						/*
						 * 경찰이 죽지 않았다면
						 */
						if (kicked[police_index] == false) {
							/*
							 * /police프로토콜을 보낸 user의 이름과 경찰의 이름이 일치한다면 경찰에게 JOB이라는 프로토콜과 유저들의 이름을 보냄
							 */
							if (name.equals(user[police_index]))
								police.println("JOB" + temp);
						}
						/*
						 * 경찰이 죽었다면
						 */
						else {
							/*
							 * 마피아와 이름이 일치하는 유저에게 NON이란느 프로토콜 보냄
							 */
							if (name.equals(user[mafia_index]))
								police.println("NON");
						}
					}

					/*
					 * 경찰이 직업을 알고싶은 사람을 선택한 경우
					 */
					else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
						PrintWriter police = info.get(user[police_index]);
						String selected = input.substring(13);
						int temp_index = 9999;
						System.out.println("selected : " + selected);

						/*
						 * 경찰이 선택한 사람과 일치하는 사람의 index 추출
						 */
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(selected) && kicked[i] == false)
								temp_index = i;
							System.out.println("user[" + i + "] : " + user[i]);
						}

						/*
						 * 경찰에게 IS_MAFIA?라는 프로토콜과 선택한 유저의 직업 정보 전달
						 */
						police.println("IS_MAFIA?" + user[temp_index] + "의 직업은"
								+ job[temp_index].substring(0, job[temp_index].indexOf(" ")) + "입니다.");
					}

					/*
					 * 마피아에게서 /kill프로토콜이 온다면
					 */
					else if (input.startsWith("/") && input.indexOf("kill") != -1) {
						PrintWriter mafia = info.get(user[mafia_index]); // 마피아의 주소 저장
						String temp = null;

						/*
						 * 마피아와 죽은 사람들을 제외한 명단 저장
						 */
						for (int i = 0; i < max_client; i++) {
							if (kicked[i] == false && !user[i].equals(user[mafia_index])) {
								if (temp == null) {
									temp = user[i];
								} else {
									temp += ("," + user[i]);
								}
							}
						}
						System.out.println("mafia " + temp);

						/*
						 * 마피아 에게 KILL이라는 프로토콜과 명단 전달
						 */
						mafia.println("KILL" + temp);
						System.out.println("마피아 명단 넘어감");

					}
					/*
					 * 마피아가 죽일 사람을 정했다면
					 */
					else if (input.startsWith("/") && input.indexOf("dead") != -1) {
						String selected = input.substring(5); // 정한 사람(victim)의 이름 추출
						PrintWriter dead = info.get(selected);
						/*
						 * 마피아에게 선택당한 사람의 index 추출
						 */
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(selected) && kicked[i] == false)
								victim_index = i;
						}

						/*
						 * 만약 의사가 살아있다면
						 */
						if (kicked[doctor_index] == false) {
							PrintWriter doctor = info.get(user[doctor_index]);
							selectedByMafia[victim_index] = true;

							String temp = null;
							/*
							 * 죽은사람 제외 자신을 포함한 명단 저장
							 */
							for (int i = 0; i < max_client; i++) {
								if (kicked[i] == false) {
									if (temp == null) {
										temp = user[i];
									} else {
										temp += ("," + user[i]);
									}
								}
							}
							/*
							 * 의사에게 DOCTOR 프로토콜과 명단 전달
							 */
							doctor.println("DOCTOR" + temp);
						}

						/*
						 * 만약 의사가 죽었다면
						 */
						else {
							/*
							 * 선택 당한 사람에게 KICKED라는 프로토콜 전달 -> 강퇴
							 */
							dead.println("KICKED");

							/*
							 * 모두에게 victim의 죽음과 직업을 broadcasting하면서 D_START -> 낮이 되었다고 알림
							 */
							if (job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("시민")
									|| job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("경찰")) {
								for (PrintWriter writer : writers) {
									writer.println("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
											+ job[victim_index].substring(0, job[victim_index].indexOf(" "))
											+ "이었 습니다.");
								}
							} else {
								for (PrintWriter writer : writers) {
									writer.println("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
											+ job[victim_index].substring(0, job[victim_index].indexOf(" "))
											+ "였 습니다.");
								}
							}
							kicked[victim_index] = true; // 강퇴당한 유저를 게임에서 제외
							current_client--; // 한명 강퇴 당함
							objectPerson(); // 오브젝트를 고를 사람을 다시 램덤으로 고름

							/*
							 * 만약 모든 오브젝트를 클릭했다면
							 */
							if (objectCount == max_object) {
								/*
								 * 모든 유저에게 T_START(타이머시작) 프로토콜을 보냄
								 */
								for (PrintWriter writer : writers) {
									writer.println("T_START" + "all object selected");
								}
							}
							/*
							 * 아직 선택할 object가 남았다면
							 */
							else {
								/*
								 * 모든 유저들에게 단서를 뽑을 사람들의 명단을 알려줌(CLUEFINEDER 프로토콜)
								 */
								for (PrintWriter writer : writers) {
									writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
											+ user[canSelect[2]]);
								}

							}

						}
					}

					/*
					 * 의사가 살릴 사람을 선택했다면
					 */
					else if (input.startsWith("/") && input.indexOf("protect") != -1) {
						PrintWriter dead = info.get(user[victim_index]);
						int temp_index = 9999;
						String protect = input.substring(8);

						/*
						 * 의사가 살린 사람의 index 추출
						 */
						for (int i = 0; i < max_client; i++) {
							if (protect.equals(user[i]))
								temp_index = i;
						}

						selectedByMafia[temp_index] = false;// 마피아에게 선택당한 것을 취소시킴 -> 원래 false인 사람을 false로 만들수도있음 -> 희생자
															// 못살리는 경우

						/*
						 * 의사가 마피아에게 선택당한 사람을 선택하지 않은 경우
						 */
						if (selectedByMafia[victim_index] == true) {
							/*
							 * 희생자 강티
							 */
							dead.println("KICKED");

							/*
							 * 나머지는 /dead 프로토콜과 비슷
							 */
							if (job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("시민")
									|| job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("경찰")) {
								for (PrintWriter writer : writers) {
									writer.println("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
											+ job[victim_index].substring(0, job[victim_index].indexOf(" "))
											+ "이었 습니다.");
								}
							} else {
								for (PrintWriter writer : writers) {
									writer.println("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
											+ job[victim_index].substring(0, job[victim_index].indexOf(" "))
											+ "였 습니다.");
								}
							}

							kicked[victim_index] = true;
							current_client--;
							objectPerson();
							if (objectCount == max_object) {
								for (PrintWriter writer : writers) {
									writer.println("T_START" + "all object selected");
								}

							} else {
								for (PrintWriter writer : writers) {
									writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
											+ user[canSelect[2]]);
								}
							}

						}

						/*
						 * 의사가 마피아에게 선택당한 사람을 선택한 경우
						 */
						else {
							/*
							 * 모든 유저에게 의사가 희생자를 살렸다고 브로드캐스트
							 */
							for (PrintWriter writer : writers) {
								writer.println("D_START" + "의사가 희생자를 지켰습니다!");
							}

							objectPerson(); // 오브젝트를 고를 사람 랜덤 선택

							if (objectCount == max_object) {
								for (PrintWriter writer : writers) {
									writer.println("T_START" + "all object selected");
								}
							} else {
								for (PrintWriter writer : writers) {
									writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
											+ user[canSelect[2]]);
								}
							}
						}
					}

					/*
					 * 유저가 오브젝트를 누른 경우
					 */
					else if (input.startsWith("object_clicked")) {
						int msg_index = Integer.parseInt(input.substring(14));

						/*
						 * 오브젝트를 누른 유저의 이름이 오브젝트를 누르도록 허용된 유저의 이름과 같다면.
						 */
						if (name.equals(user[canSelect[clickedNum]])) {

							/*
							 * 오브젝트가 선택되지 않았고 (object_flag[msg_index] == true ) 해당 유저가 다른 오브젝트를 클릭 하지
							 * 않았다면(isClicked[canSelect[clickedNum]] == false)
							 */
							if (object_flag[msg_index] == true && isClicked[canSelect[clickedNum]] == false) {
								PrintWriter sendObject = info.get(name);

								System.out.println(msg_index);

								/*
								 * 첫번째 두번째 유저가 오브젝트를 선택하는 경우
								 */
								if (clickedNum != selectNum - 1) {

									/*
									 * 모든 유저에게 해당 유저가 오브젝트를 뽑았음을 알려주고 다음 유저가 오브젝트를 뽑을 차례라는 것을 알려줌
									 */
									for (PrintWriter writer : writers) {

										/*
										 * 아직 선택할 오브젝트가 남아 있는 경우
										 */
										if (objectCount != max_object - 1) {
											writer.println("FOUND" + user[canSelect[clickedNum]] + ","
													+ user[canSelect[clickedNum + 1]]);
										}
										/*
										 * 해당 유저가 오브젝트를 뽑아서 더이상 선택할 오브젝트가 없는 경우(10개 모두 선택한 경우)
										 */
										else {
											writer.println(
													"FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
										}

									}
									sendObject.println("object_description" + object_msg[msg_index]); // 사용자가 누른 오브젝트에
																										// 해당하는 메세지 전송
									object_flag[msg_index] = false; // 해당 오브젝틑 비활성화
									clickedNum++; // 클릭한 횟수 증가

								}

								/*
								 * 마지막 유저가 오브젝트를 선택하는 경우
								 */
								else {
									for (PrintWriter writer : writers) {
										writer.println("FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
									}
									sendObject.println("object_description" + object_msg[msg_index]);
									object_flag[msg_index] = false;
									clickedNum++;
								}
								objectCount++;

								/*
								 * 해당 유저가 오브젝트를 선택해서 더이상 선택할 오브젝트가 없는 경우(10개 모두 선택)
								 */
								if (objectCount == max_object) {
									for (PrintWriter writer : writers) {
										writer.println("T_START" + "all object selected");
									}
								}
							}

							/*
							 * 이미 다른 사람이 해당 오브젝트를 선택한 경우 -> 해당 유저는 다른 오브젝트를 뽑을 수 있음
							 */
							else if (object_flag[msg_index] == false && isClicked[canSelect[clickedNum]] == false) {
								PrintWriter sendObject = info.get(name);
								sendObject.println("object_description" + "이미 다른 사람이 선택한 오브젝트입니다.");
							}
						}

						/*
						 * 해당 유저의 차례가 아닌 경우
						 */
						else {
							PrintWriter sendObject = info.get(name);
							sendObject.println("object_description" + "당신의 차례가 아닙니다");
						}

						/*
						 * 오브젝트를 뽑도록 지정된 유저(3명)가 다 뽑은 경우
						 */
						if (clickedNum == selectNum) {

							/*
							 * 모든 유저들에게 타이머 시작 프로토콜 보냄
							 */
							for (PrintWriter writer : writers) {
								writer.println("T_START");
							}
							clickedNum = 0;

							/*
							 * 다시 초기화 -> 다음 턴에 오브젝트를 뽑을 유저들의 인덱스를 저장해야 하기 때문
							 */
							for (int i = 0; i < selectNum; i++) {
								canSelect[i] = 9999;
							}
						}

					}

					/*
					 * 그냥 채팅 치는 경우
					 */
					else {
						for (PrintWriter writer : writers) {
							if (!input.equals("")) // 엔터키만 계속 누르면 메세지 없지 공백문자만 출력되는 경우를 제외하고
								writer.println("MESSAGE " + name + ": " + input);
						}
					}

					/*
					 * 모든 유저들이 투표를 마쳤다면
					 */
					if (is_vote == client_count) {
						int count = 0;
						int temp_index = 0;
						int same = 0;

						/*
						 * 가장 많이 표를 받은 유저를 찾아냄
						 */
						for (int i = 0; i < max_client; i++) {
							if (vote[i] > count) {
								count = vote[i];
								temp_index = i;
							}
						}

						/*
						 * 한번 더 검사해서 동률이 있는지 찾아냄
						 */
						for (int i = 0; i < max_client; i++) {
							if (count == vote[i] && i != temp_index)
								same = 1;
						}

						if (count == 0)
							same = 0;

						/*
						 * 동률이 아닌 경우
						 */
						if (same != 1) {
							PrintWriter victim = info.get(user[temp_index]);
							victim.println("KICKED");
							for (PrintWriter writer : writers) {
								writer.println("V_END" + user[temp_index] + " dead, he was "
										+ job[temp_index].substring(0, job[temp_index].indexOf(" ")));
							}
							kicked[temp_index] = true;
							current_client--;
						}

						/*
						 * 동률인 경우
						 */
						else {
							for (PrintWriter writer : writers) {
								writer.println("V_END" + "아무도 처형당하지 않았습니다.");
							}
						}
						is_vote = 0;
						count = 0;

						/*
						 * 다음 투표를 위해 초기화
						 */
						for (int i = 0; i < max_client; i++)
							vote[i] = 0;
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (name != null) {
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

	public static void sendToallclient(String mssg) {
		for (PrintWriter writer : writers) {
			writer.println(mssg);
			writer.flush();
		}
	}
}