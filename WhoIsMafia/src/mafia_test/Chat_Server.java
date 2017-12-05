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
	private static final int PORT = 9001;
	private static HashSet<String> names = new HashSet<String>();
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>();
	private static int max_client = 7;
	private static int max_object = 10;
	private static int client_count = 0;
	private static int matrix_size = 7;
	private static int num_of_clue = 7;
	private static int selectNum = 2;
	private static int storedIndex = 0;

	private static int timer_flag = 0;
	private static int clickedNum = 0;
	private static int current_client = max_client;
	private static int is_vote = 0;
	private static int mafia_index = 0;
	private static int police_index = 0;
	private static int doctor_index = 0;
	private static int victim_index = 0;
	private static int[] selectedByMafia = new int[max_client];
	private static int[] kicked = new int[max_client];
	private static int[] isClicked = new int[max_client];
	private static int[] vote = new int[max_client];
	private static int[] object_flag = new int[max_object];
	private static String[] object_msg = { "0번입니다", "1번입니다", "2번입니다", "3번입니다", "4번입니다", "5번입니다", "6번입니다", "7번입니다",
			"8번입니다", "9번입니다" };

	private static String[] user = new String[max_client];
	private static PrintWriter[] ID = new PrintWriter[max_client];
	private static String[] job = { "citizen i'm citizen", "doctor i'm doctor", "citizen i'm citizen",
			"mafia i'm mafia", "citizen i'm citizen", "police i'm police", "citizen i'm citizen" };

	private static int[] random = { -1, -1, -1, -1, -1, -1, -1 };

	private static void initialize() {
		for (int i = 0; i < max_client; i++) {
			kicked[i] = 1;
			selectedByMafia[i] = 0;
			vote[i] = 0;
			isClicked[i] = 0;
			user[i] = "null";
			ID[i] = null;
		}

		for (int i = 0; i < max_object; i++)
			object_flag[i] = 0;
	}

	private static void randomArray() {
		int index = 0;
		int matrix_count = 0;
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

				if (client_count == max_client) {
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + "game start");
					}
					for (int i = 0; i < max_client; i++) {
						String temp = job[i];
						job[i] = job[random[i]];
						job[random[i]] = temp;
					}

					for (int i = 0; i < max_client; i++) {
						if ((job[i].substring(0, job[i].indexOf(" "))).equals("mafia"))
							mafia_index = i;
						else if ((job[i].substring(0, job[i].indexOf(" "))).equals("police"))
							police_index = i;
						else if ((job[i].substring(0, job[i].indexOf(" "))).equals("doctor"))
							doctor_index = i;
					}
					for (int i = 0; i < max_client; i++)
						System.out.println(job[i]);
				}

				while (true) {
					String input = in.readLine();

					if (input == null) {
						return;
					} else if (input.startsWith("<") && input.indexOf("/>") != -1) {
						String whisper;
						whisper = input.substring(1, input.indexOf("/>"));

						if (names.contains(whisper)) {
							PrintWriter sender = info.get(name);
							PrintWriter receiver = info.get(whisper);
							receiver.println("MESSAGE " + "<whisper from " + name + "> : "
									+ input.substring(whisper.length() + 3));
							sender.println("MESSAGE " + "<whisper to " + whisper + "> : "
									+ input.substring(whisper.length() + 3));
						} else {
							PrintWriter sender = info.get(name);
							sender.println("MESSAGE " + "This user does not exist.");
						}

					}

					else if (input.startsWith("/") && input.indexOf("job") != -1) {
						int temp_index = 0;

						for (int i = 0; i < client_count; i++) {
							if (name == user[i])
								temp_index = i;
						}

						PrintWriter sender = info.get(name);
						sender.println("MESSAGE " + "your job is "
								+ job[temp_index].substring(0, job[temp_index].indexOf(" ")));
					}

					else if (input.startsWith("/") && input.indexOf("role") != -1) {
						int temp_index = 0;
						for (int i = 0; i < client_count; i++) {
							if (name == user[i])
								temp_index = i;
						}
						PrintWriter sender = info.get(name);
						sender.println(
								"MESSAGE " + "your role is " + job[temp_index].substring(job[temp_index].indexOf(" ")));
					}

					else if (input.startsWith("/") && input.indexOf("timeout") != -1) {
						timer_flag++;
						System.out.println(timer_flag);
						if (timer_flag == current_client) {
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
							timer_flag = 0;
						}
					} else if (input.startsWith("/") && input.indexOf("victim") != -1) {
						String victim = input.substring(7);
						int temp_index = 0;
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(victim) && kicked[i] != 0)
								temp_index = i;
						}
						is_vote++;
						vote[temp_index]++;
					} else if (input.startsWith("/") && input.indexOf("police") != -1) {
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
						if (kicked[police_index] == 1) {
							if (name.equals(user[police_index]))
								police.println("JOB" + temp);
						} else {
							if (name.equals(user[mafia_index]))
								police.println("NON");
						}
					} else if (input.startsWith("/") && input.indexOf("kill") != -1) {
						System.out.println("마피아냐?");
						PrintWriter mafia = info.get(user[mafia_index]);
						String temp = null;
						for (int i = 0; i < max_client; i++) {
							if (kicked[i] != 0 && !user[i].equals(user[mafia_index])) {
								if (temp == null) {
									temp = user[i];
								} else {
									temp += ("," + user[i]);
								}
							}
						}
						System.out.println("mafia " + temp);

						mafia.println("KILL" + temp);
						System.out.println("마피아 명단 넘어감");

					} else if (input.startsWith("/") && input.indexOf("dead") != -1) {
						PrintWriter mafia = info.get(user[mafia_index]);
						String selected = input.substring(5);
						PrintWriter dead = info.get(selected);
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(selected) && kicked[i] != 0)
								victim_index = i;
						}
						if (kicked[doctor_index] != 0) {
							PrintWriter doctor = info.get(user[doctor_index]);
							selectedByMafia[victim_index] = 1;

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
							doctor.println("DOCTOR" + temp);
						} else {
							dead.println("KICKED");
							for (PrintWriter writer : writers) {
								writer.println("D_START" + user[victim_index] + " dead, he was "
										+ job[victim_index].substring(0, job[victim_index].indexOf(" ")));
							}
							kicked[victim_index] = 0;
							current_client--;
						}
					} else if (input.startsWith("/") && input.indexOf("protect") != -1) {
						PrintWriter dead = info.get(user[victim_index]);
						int temp_index = 9999;
						String protect = input.substring(8);

						for (int i = 0; i < max_client; i++) {
							if (protect.equals(user[i]))
								temp_index = i;
						}

						selectedByMafia[temp_index] = 0;

						if (selectedByMafia[victim_index] == 1) {
							dead.println("KICKED");
							for (PrintWriter writer : writers) {
								writer.println("D_START" + user[victim_index] + " dead, he was "
										+ job[victim_index].substring(0, job[victim_index].indexOf(" ")));
							}
							kicked[victim_index] = 0;
							current_client--;
						} else {
							for (PrintWriter writer : writers) {
								writer.println("D_START" + "Doctor saved victim");
							}
						}
					}
					// else if (input.startsWith("/") && input.indexOf("police") != -1) {
					// PrintWriter police = info.get(name);
					// String temp = null;
					// for (int i = 0; i < max_client; i++) {
					// if (kicked[i] != 0 && !user[i].equals(user[police_index])) {
					// if (temp == null) {
					// temp = user[i];
					// } else {
					// temp += ("," + user[i]);
					// }
					// }
					// }
					// System.out.println(temp);
					// if (name.equals(user[police_index])) {
					// police.println("JOB" + temp);
					// } else {
					// police.println("MESSAGE " + "You are not police");
					// }
					// }
					else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
						PrintWriter police = info.get(user[police_index]);
						String selected = input.substring(13);
						int temp_index = 9999;
						System.out.println("selected : " + selected);
						for (int i = 0; i < max_client; i++) {
							if (user[i].equals(selected) && kicked[i] != 0)
								temp_index = i;
							System.out.println("user[" + i + "] : " + user[i]);
						}
						police.println("IS_MAFIA?" + user[temp_index] + "' job is "
								+ job[temp_index].substring(0, job[temp_index].indexOf(" ")));
					} else if (input.startsWith("object_clicked")) {
						int msg_index = Integer.parseInt(input.substring(14));
						int user_index = 0;
						for (int i = 0; i < max_client; i++) {
							if (name.equals(user[i]))
								user_index = i;
						}
						if (object_flag[msg_index] == 0 && isClicked[user_index] == 0) {
							isClicked[user_index] = 1;
							object_flag[msg_index] = 1;
							clickedNum++;
							PrintWriter sendObject = info.get(name);

							System.out.println(msg_index);
							sendObject.println("object_description" + object_msg[msg_index]);
							if (clickedNum == current_client) {
								for (PrintWriter writer : writers) {
									writer.println("T_START");
								}
								for (int i = 0; i < max_client; i++)
									isClicked[i] = 0;
								clickedNum = 0;
							}
						} else if (object_flag[msg_index] == 1 && isClicked[user_index] == 0) {
							PrintWriter sendObject = info.get(name);
							sendObject.println("object_description" + "이미 다른 사람이 선택한 오브젝트입니다.");
						} else if (isClicked[user_index] == 1) {
							PrintWriter sendObject = info.get(name);
							sendObject.println("object_description" + "당신은 이미 하나의 오브젝트를 선택하였습니다");
						}
					} else {
						for (PrintWriter writer : writers) {
							writer.println("MESSAGE " + name + ": " + input);
						}
					}

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
							PrintWriter victim = info.get(user[temp_index]);
							victim.println("KICKED");
							for (PrintWriter writer : writers) {
								writer.println("V_END" + user[temp_index] + " dead, he was "
										+ job[temp_index].substring(0, job[temp_index].indexOf(" ")));
							}
							kicked[temp_index] = 0;
							current_client--;
						} else {
							for (PrintWriter writer : writers) {
								writer.println("V_END" + "Nothing happened");
							}
						}
						is_vote = 0;
						count = 0;
						for (int i = 0; i < max_client; i++)
							vote[i] = 0;
					}

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
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (name != null) {
					// for (PrintWriter writer : writers) {
					// writer.println("MESSAGE " + "[" + name + "] exit");
					// }
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