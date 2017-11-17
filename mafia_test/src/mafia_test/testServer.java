package mafia_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class testServer {
	private static final int PORT = 9001;
	private static HashSet<String> names = new HashSet<String>();
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>();

	/********************************************
	 * newly added variable
	 *********************************************************************/
	private static int max_client = 7;
	private static int client_count = 0;
	private static int[] gaming = new int[max_client];
	private static String[] user = new String[max_client];
	private static PrintWriter[] ID = new PrintWriter[max_client];
	private static String[] job = new String[max_client];
	private static String[] temp_job = { "mafia i'm mafia", "doctor i'm doctor", "police i'm police",
			"citizen i'm citizen", "citizen i'm citizen", "citizen i'm citizen", "citizen i'm citizen" };
	private static int[] random = { -1, -1, -1, -1, -1, -1, -1 };

	/*****************************************************************************************************************/

	/********************************************
	 * newly added function
	 *********************************************************************/
	private static void initialize(String job[], String[] user, PrintWriter[] ID, int[] gaming) {
		for (int i = 0; i < max_client; i++) {
			job[i] = "not yet decided";
			user[i] = "";
			ID[i] = null;
			gaming[i] = 0;
		}
	}

	private static void randomArray(int[] random) {
		int index = 0;
		while (true) {
			int value = (int) (Math.random() * max_client);
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

	private static void whisper(String input, String name) {
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

	private static void showJob(String name) {
		int temp_index = 0;
		for (int i = 0; i < client_count; i++) {
			if (name == user[i])
				temp_index = i;
		}
		PrintWriter sender = info.get(name);
		sender.println("MESSAGE " + "your job is " + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
	}

	private static void showRole(String name) {
		int temp_index = 0;
		for (int i = 0; i < client_count; i++) {
			if (name == user[i])
				temp_index = i;
		}
		PrintWriter sender = info.get(name);
		sender.println("MESSAGE " + "your role is " + job[temp_index].substring(job[temp_index].indexOf(" ")));
	}

	private static void kick(String input, String name) {
		String kickedUser = input.substring(6);
		int temp_index = 0;
		int count = 0;
		for (int i = 0; i < user.length; i++) {
			if (kickedUser.equals(user[i])) {
				count++;
				temp_index = i;
			}
		}
		PrintWriter sender = info.get(name);
		PrintWriter receiver = info.get(kickedUser);

		if (count != 0) {
			sender.println("MESSAGE " + kickedUser + " is kicked");
			receiver.println("MESSAGE " + "you were kicked from this game by " + name);
			receiver.println("KICKED");
			gaming[temp_index] = 0;

		} else
			sender.println("MESSAGE " + kickedUser + " is not exist");
	}

	private static void broadcast(String input, String name) {
		for (PrintWriter writer : writers) {
			writer.println("MESSAGE " + name + ": " + input);
		}
	}

	private static void setJobRandomly(String[] job) {
		if (client_count == max_client) {
			randomArray(random);
			for (int i = 0; i < max_client; i++)
				System.out.println(random[i]);
			for (PrintWriter writer : writers) {
				writer.println("MESSAGE " + "game start");
			}
			for (int i = 0; i < max_client; i++)
				job[i] = temp_job[random[i]];
			for (int i = 0; i < max_client; i++) {

				System.out.println("random " + job[i]);
			}
		}
	}

	/*****************************************************************************************************************/
	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT);
		initialize(job, user, ID, gaming);
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
				/**********************************************
				 * newly added code
				 *******************************************************************/
				writers.add(out);
				user[client_count] = name;
				ID[client_count] = out;
				gaming[client_count] = 1;
				client_count++;
				System.out.println("한명 들어왔다 " + client_count);
				info.put(name, out);

				setJobRandomly(job);

				/*****************************************************************************************************************/
				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					} else if (input.startsWith("<") && input.indexOf("/>") != -1) {
						whisper(input, name);
					} else if (input.startsWith("/") && input.indexOf("job") != -1) {
						showJob(name);
					} else if (input.startsWith("/") && input.indexOf("role") != -1) {
						showRole(name);
					} else if (input.startsWith("/") && input.indexOf("kick") != -1) {
						kick(input, name);
					} else {
						broadcast(input, name);
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (name != null) {
					/************************************************************/
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + "[" + name + "] exit");
					}
					names.remove(name);
					info.remove(name);
					client_count--;
					System.out.println("한명 나갔다 " + client_count);
					/************************************************************/
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
}