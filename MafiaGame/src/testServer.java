

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

   private static int max_client = 7;
   private static int client_count = 0;
   private static int[] vote = new int[max_client];
   private static String[] user = new String[max_client];
   private static PrintWriter[] ID = new PrintWriter[max_client];
   private static String[] job = { "mafia i'm mafia", "citizen i'm citizen", "citizen i'm citizen",
         "citizen i'm citizen", "citizen i'm citizen", "doctor i'm doctor", "police i'm police" };

   private static int[] random = { -1, -1, -1, -1, -1, -1, -1 };

   private static void initialize(int[] vote, String[] user, PrintWriter[] ID) {
      for (int i = 0; i < max_client; i++) {
         vote[i] = 0;
         user[i] = "";
         ID[i] = null;
      }
   }

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

   public static void main(String[] args) throws Exception {
      System.out.println("The chat server is running.");
      ServerSocket listener = new ServerSocket(PORT);
      randomArray(random);
      initialize(vote, user, ID);
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
            writers.add(out);
            user[client_count] = name;
            ID[client_count] = out;
            client_count++;
            System.out.println("한명 들어왔다 " + client_count);
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

               } else if (input.startsWith("/") && input.indexOf("job") != -1) {
                  int temp_index = 0;
                  for (int i = 0; i < client_count; i++) {
                     if (name == user[i])
                        temp_index = i;
                  }
                  PrintWriter sender = info.get(name);
                  sender.println("MESSAGE " + "your job is "
                        + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
               } else if (input.startsWith("/") && input.indexOf("role") != -1) {
                  int temp_index = 0;
                  for (int i = 0; i < client_count; i++) {
                     if (name == user[i])
                        temp_index = i;
                  }
                  PrintWriter sender = info.get(name);
                  sender.println(
                        "MESSAGE " + "your role is " + job[temp_index].substring(job[temp_index].indexOf(" ")));
               } else if (input.startsWith("/") && input.indexOf("vote") != -1) {
                  int temp_index = 0;
                  for (int i = 0; i < client_count; i++) {
                     if (name == user[i])
                        temp_index = i;
                  }
               }

               else {
                  for (PrintWriter writer : writers) {
                     writer.println("MESSAGE " + name + ": " + input);
                  }
               }
            }
         } catch (IOException e) {
            System.out.println(e);
         } finally {
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
}