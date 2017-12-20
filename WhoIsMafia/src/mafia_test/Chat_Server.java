package net_hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
/**********************************************************************************************************
 * class Chat_Server
 * 
 * Wait until the 7 players gather.
 * After gathering, assign a job to all players.
 * Alert the starting of game and pick a people who can pick object.
 * After 5 minutes after finding object, tell players(clients) to vote to kill.
 * Alert result and do working of police, Mafia and doctor until whether Mafia or citizen is win.
 *
 * @PORT[integer]: port number
 * @names[string]: store names for no duplication
 * @writers[printWriter]: store address of client for no duplication
 * @info[string,printWriter]: map between name and address of hosts
 * @max_client[integer]: the maximum number of clients
 * @max_object[integer]: the number of objects, clue
 * @client_count[integer]: the number of players who are entering now
 * @time_flag[integer]: the number of clients who send timeout
 * @clickedNum[integer]: the number of players who can click the object in one turn.
 * @current_client[integer]: the number of players in now who are not kicked.
 * @is_vote[integer]: the number of players who vote(==current_client: calculate kicked-person)
 * @mafia_index[integer]: the index of Mafia
 * @police_index[integer]: the index of police
 * @doctor_index[integer]: the index of doctor
 * @victim_index[integer]: the index of voted person by Mafia at night
 * @temp[integer]: array for removing duplication of index in putting name to player's room in clue-map
 * @selecterByMafia[boolean]: check whether the Mafia kicked me or not(true: checked, false:no checked)
 * @kicked[integer]: check whether i am kicked of not(true: kicked, false: no kicked)
 * @vote[integer]: sum of voted number for each player
 * @user[string]: store name of player for mapping with index
 * @ID[printWriter]: store addresses of player for mapping with index
 * @story[string]: story in game for clue-background
 * @totalJob[string]: tell a job by button
 * @job[string]: array of storing jobs
 * @random[integer]: array about random index for mapping with index
 * @footSize[integer]: clue for finding Mafia with foot size
 * @selectNum[integer]: the number of players who can pick object
 * @objectCount[integer]: the number of clicked-object(==max_object: no finding object)
 * @canSelect[integer]: check whether each player can pick object or not
 * @isClicked[boolean]: check whether player clicked object or not
 * @object_flag[boolean]: check whether each object was clicked or not
 * @object_msg[string]: the object message
 **********************************************************************************************************/
public class Chat_Server {
   private static final int PORT = 9001; // port number
   private static HashSet<String> names = new HashSet<String>(); // hashset that stores user's name -> prevent same username
   private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); // hashset that stores client's ip address -> also prevent same ip
   private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>(); // hashmap that maps username and user's ip address
                                                                     // hashmap
   private static int max_client = 7; // variable max client number
   private static int max_object = 10; // max number of object -> objects describe the items in the room map
   private static int client_count = 0; // current user numbers in game

   private static int game_start_flag = 0; // flag variable checks if game is started
   private static int timer_flag = 0; // timeout -> count user number timeout happens
   private static int clickedNum = 0; // in a turn only 3 users can click the object 
   private static int current_client = max_client; // current user number // current client indicates users who are not kicked

   private static int is_vote = 0; // number of users who voted 
   private static int mafia_index = 0; //mafia's index
   private static int police_index = 0; // police's index
   private static int doctor_index = 0; // doctor's index
   private static int victim_index = 0; // victim's index
   private static int[] temp = new int[] { -1, -1, -1 }; // when allocating users in room1 and room2 -> not to make redundant

   private static boolean[] selectedByMafia = new boolean[max_client]; // if user is selected by mafia
                                                      
   private static boolean[] kicked = new boolean[max_client]; //if the user is kicked or not

   private static int[] vote = new int[max_client]; // how many vote num the user got

   private static String[] user = new String[max_client]; // store user name -> map with index (hashmap)
   private static PrintWriter[] ID = new PrintWriter[max_client]; // store user's address -> map with index(hashmap)
                                                   // 위해
   private static String story = "이곳은 외딴 섬의 펜션/우리 여덟명은 누군가의 초대로 이 곳에 모였다./그러나 어제 저녁 거실에서 한 구의 시체가 발견되었다./우리는 이것이 어떤 미친 마피아의 소행이라고 생각한다./"
         + "펜션의 모든 출입문은 안에서부터 굳게 닫혀있다./필시 마피아가 열쇠를 가지고 있을 것이다./"
         + "더 많은 희생자가 나오기 전에/그리고 이 펜션을 탈출하기 위해/우리는 마피아를 찾아 구속해야 한다.../"
         + "그러기 위해 우리는 지금 거실에 모였다./펜션 안에 흩어진 단서와 증거들을 수색해 마피아를 찾아내고 이 곳을 탈출하자."; // story
                                                               // store
   private static String totalJob = ""; //tells all job information
   private static String[] job = { "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요!/마피아를 검거하면 당신들의 승리입니다.",
         "의사 밤에 마피아에게 죽임을 당할 것 같은 시민을 살리세요!/시민이 모두 죽으면 마피아가 승리할 것 입니다.",
         "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요!/마피아를 검거하면 당신들의 승리입니다.",
         "마피아 마피아는 단서에 쓰여진 당신의 정보를 숨겨야합니다./정보가 드러나지 않게 주의하세요.",
         "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요!/마피아를 검거하면 당신들의 승리입니다.",
         "경찰 밤에 당신이 알고싶은 플레이어의 직업을 알아볼 수 있습니다./시민이 승리할 수 있도록 토론을 이끌어보세요.",
         "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요!/마피아를 검거하면 당신들의 승리입니다." }; // stores job information

   private static int[] random = { -1, -1, -1, -1, -1, -1, -1 }; // to make index randomly -> random array -> map with index

   private static int[] footSize = { 245, 250, 255, 260, 265, 270, 275 };
   private static int selectNum = 3; // number of user that can click object
   private static int objectCount = 0; // number of user clicked object
   private static int[] canSelect = new int[selectNum]; // check if user can select object
   private static boolean[] isClicked = new boolean[max_client]; // check if user clicked object
   private static boolean[] object_flag = new boolean[max_object]; // if object is selected or not
   private static String[] object_msg = { "경찰에 대한 정보", "의사에 대한 정보", "피 묻은 발자국이 서쪽 방으로 이어져 있다. 말굽 모양의 발자국이다...",
         "마피아 방에 대한 정보", "마피아가 없는 방에 대한 정보", "전체 발사이즈", "마피아 발사이즈", "'오늘 야식 메뉴 - 라면'이 적혀있는 쪽지이다.",
         "뭐지 이 문서는?? 텀 프로젝트..? 뉴럴 네트워크...? 잘 모르는 내용이다.", "어제 저녁의 재료 목록이다. 양고기, 후추, 미림..." };

   private static String user_map_job = null;

   /**********************************************************************************************************
    * void objectPerson
    * 
    * pick 3 players who can pick object and make them to pick
    * 
    * @count[integer]: check 3 players
    * @cnt[integer]: checking for duplication
    * @client_value[integer]: random number
    **********************************************************************************************************/

   private static void objectPerson() {
      int count = 0;
      while (true) {
         int client_value = (int) (Math.random() * max_client); // client_value에 0~6까지 랜덤 변수 지정
         int cnt = 0;

         /*
          * canSelect array check if there are same as client_check value, if there are same values increase cnt ++ 
          */
         for (int i = 0; i < selectNum; i++) {
            if (client_value == canSelect[i])
               cnt++;
         }

         /*
          * cnt is 0 -> no redundant value. kicked[client_value] is false? -> 오브젝트를 뽑을 사람의
          * index를 client_value값으로 하는데, false라는 것은 해당 유저가 강퇴당하지 않았다는 것이다.
          */
         if (cnt == 0 && kicked[client_value] == false) {
            canSelect[count] = client_value;
            count++;
         }

         /*
          * if all 3 users clicked objects
          */
         if (count == selectNum)
            break;
      }
   }

   /*
    * initalize variables
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
       * story button information
       */
      totalJob = "," + job[0].substring(0, job[0].indexOf(" ")) + "," + job[0].substring(job[0].indexOf(" ") + 1)
            + "," + job[1].substring(0, job[1].indexOf(" ")) + "," + job[1].substring(job[1].indexOf(" ") + 1) + ","
            + job[3].substring(0, job[3].indexOf(" ")) + "," + job[3].substring(job[3].indexOf(" ") + 1) + ","
            + job[5].substring(0, job[5].indexOf(" ")) + "," + job[5].substring(job[5].indexOf(" ") + 1);
   }

   /*
    * random array -> make no redundancy
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

   private static void name_map_job() {
      for (int i = 0; i < max_client; i++) {
         if (user_map_job == null)
            user_map_job = user[i] + "," + job[i] + ",";
         else {
            if (i == max_client - 1)
               user_map_job += user[i] + "," + job[i];
            else
               user_map_job += user[i] + "," + job[i] + ",";
         }
      }
   }

   /*
    * main job's index
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
    * police and doctor's information
    */
   private static void assignClue_pol_doc() {
      object_msg[0] = "소파 틈새에서 " + user[police_index] + "의 이름이 적혀있는 경찰 뱃지를 발견했다.";
      object_msg[1] = "피아노 위에 병원 출입 카드가 있다. " + user[doctor_index] + "가 어제 저녁, 피아노를 연주할 때 옆에 놔 둔 것 같다.";
   }

   /*
    * list of user who stayed with mafia
    */
   private static void assignClue_room1() {
      object_msg[3] = "어제 이 방에서 잤던 사람들은 " + user[mafia_index];
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
      object_msg[3] += "이다.";
   }

   /*
    * list all of user of room( not mafia's room)
    */
   private static void assignClue_room2() {
      object_msg[4] = "어제 이 방에서 잤던 사람들은";
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
      object_msg[4] += "이다.";
   }

   /*
    * every user's footsize
    */
   private static void assignClue_totalFootSize() {
      object_msg[5] = "foot size," + user[0] + " : " + footSize[0] + "," + user[1] + " : " + footSize[1] + ","
            + user[2] + " : " + footSize[2] + "," + user[3] + " : " + footSize[3] + "," + user[4] + " : "
            + footSize[4] + "," + user[5] + " : " + footSize[5] + "," + user[6] + " : " + footSize[6];
   }

   /*
    * mafia's foot size
    */
   private static void assignClue_mafiaFootSize() {
      object_msg[6] = "말굽 모양의 구두다. 사이즈는" + (footSize[mafia_index] - 5) + " ~ " + (footSize[mafia_index] + 5)
            + "정도려나?";
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
               if (game_start_flag == 0) {
                  out.println("SUBMITNAME");
                  name = in.readLine();
                  if (name == null) {
                     return;
                  }
                  synchronized (names) {
                     sendToallclient("CONNECT " + name + " is connected.\n");

                     if (!names.contains(name)) {
                        names.add(name);

                        sendToallclient("MESSAGE " + "새로운 유저 '" + name + "' 님이 입장하셨습니다.");
                        break;
                     } else {
                        out.println("ERROR " + "이미 사용중인 닉네임입니다.");
                     }
                  }
               }
            }
            out.println("NAMEACCEPTED");

            writers.add(out);
            out.println("MESSAGE " + "[게임에 입장하셨습니다]");
            user[client_count] = name;
            ID[client_count] = out;

            client_count++;

            info.put(name, out);
            if (client_count == max_client) {
               game_start_flag = 1;
               name_map_job();
               sendToallclient("FLAG");// broadcast that game has started
               objectPerson();
               sendToallclient("MESSAGE " + "게임이 시작되었습니다.");
               sendToallclient("MESSAGE " + "현재 시간은 낮이므로");
               sendToallclient("MESSAGE " + "수색을 맡은 사람들이 단서를 찾아주세요.");
               sendToallclient(
                     "CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + "," + user[canSelect[2]]);

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
                  sendToallclient("ENDMESSAGE " + "시민들은 마피아를 색출해 성공적으로 구속했습니다. 시민의 승리입니다!");
                  System.exit(0);
               } else if (current_client == 2) {
                  sendToallclient("ENDMESSAGE " + "시민들은 결국 마피아를 찾아내지 못했습니다. 남은 시민도 곧 마피아에게 죽을 것입니다. 마피아의 승리입니다!");
                  System.exit(0);
               }
               String input = in.readLine();
               if (input == null) {
                  return;
               }

               /*
                * user clicked job button in room map -> handle
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
                * user clicked story protocol 
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
                * timer_flag -> increase if each users timer is finished
                */
               else if (input.startsWith("/") && input.indexOf("timeout") != -1) {
                  timer_flag++;
                  /*
                   *if every users' timer is finished
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
                     /*
                      * send all users VOTENAME protocol -> send user the username to kill
                      */
                     for (PrintWriter writer : writers) {

                        writer.println("VOTENAME " + temp);
                     }
                     timer_flag = 0; // 다음 투표를 위해 0으로 초기화
                  }
               }

               /*
                * users vote -> store each value that users vote
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
                * night time -> execute police's role
                */
               else if (input.startsWith("/") && input.indexOf("police") != -1) {
                  PrintWriter police = info.get(name);
                  String temp = null;

                  /*
                   * store index except for police
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
                  
                  /*
                   * if police is not dead
                   */
                  if (kicked[police_index] == false) {
                     /*
                      * /if user name is same as the user who got POLICE protocol send JOB to that user
                      */
                     if (name.equals(user[police_index]))
                        police.println("JOB" + temp);
                  }
                  /*
                   * if police is dead
                   */
                  else {
                     /*
                      * send NON protocol to user that is same with mafia name
                      */
                     if (name.equals(user[mafia_index]))
                        police.println("NON");
                  }
               }

               /*
                * if police choose the user that he wants to know
                */
               else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
                  PrintWriter police = info.get(user[police_index]);
                  String selected = input.substring(13);
                  int temp_index = 9999;

                  /*
                   * extract index of user that police choose
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        temp_index = i;
                  }

                  /*
                   * send to police is_MAFIA protocol and job of chosen user
                   */
                  police.println("IS_MAFIA?" + user[temp_index] + "의 직업은"
                        + job[temp_index].substring(0, job[temp_index].indexOf(" ")) + "입니다.");
               }

               /*
                * kill protocol from mafia
                */
               else if (input.startsWith("/") && input.indexOf("kill") != -1) {
                  PrintWriter mafia = info.get(user[mafia_index]); // mafia's address
                  String temp = null;

                  /*
                   * index that stores user except for dead users
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

                  /*
                   * send mafia KILL protocol
                   */
                  mafia.println("KILL" + temp);

               }
               /*
                * if mafia decide user to kill
                */
               else if (input.startsWith("/") && input.indexOf("dead") != -1) {
                  String selected = input.substring(5); // 
                  PrintWriter dead = info.get(selected);
                  /*
                   * extract victim_index
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        victim_index = i;
                  }

                  /*
                   * if doctor is alive
                   */
                  if (kicked[doctor_index] == false) {
                     PrintWriter doctor = info.get(user[doctor_index]);
                     selectedByMafia[victim_index] = true;

                     String temp = null;
                     /*
                      * exclude kicked user in saving index
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
                      * send doctor DOCTOR protocol and the username to save
                      */
                     doctor.println("DOCTOR" + temp);
                  }

                  /*
                   * if the doctor is dead
                   */
                  else {
                     /*
                      * send kicked protocol to kicked user 
                      */
                     dead.println("KICKED" + user_map_job);

                     /*
                      * broadcast every user victim's nickname and job and tell that the day start 
                      */
                     if (job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("시민")
                           || job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("경찰")) {
                        sendToallclient("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은 "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")) + "입니다.");
                     } else {
                        sendToallclient("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은 "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")) + "입니다.");
                     }
                     kicked[victim_index] = true; // exclude kicked user
                     current_client--; // one user is kicked
                     objectPerson(); // randomly choose user to pick object

                     /*
                      * if every objects are clicked
                      */
                     if (objectCount == max_object) {
                        /*
                         * send every user timer_start protocol
                         */
                        sendToallclient("T_START" + "all object selected");
                     }
                     /*
                      * if there are stil remaining user who has to find object
                      */
                     else {
                        /*
                         *broadcast every user that which users have to choose clue(CLUEFINEDER protocol)
                         */
                        sendToallclient("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                              + user[canSelect[2]]);

                     }

                  }
               }

               /*
                * if the doctor choose user to save
                */
               else if (input.startsWith("/") && input.indexOf("protect") != -1) {
                  PrintWriter dead = info.get(user[victim_index]);
                  int temp_index = 9999;
                  String protect = input.substring(8);

                  /*
                   * index that doctor saved
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (protect.equals(user[i]))
                        temp_index = i;
                  }

                  selectedByMafia[temp_index] = false;// cancel what mafia choose -> victim
                                             // case that cannot save victim

                  /*
                   * case when the user chosen by doctor and user mafia trying to kill is different
                   */
                  if (selectedByMafia[victim_index] == true) {
                     /*
                      * kick victim
                      */
                     dead.println("KICKED" + user_map_job);

                     /*
                      * same as /dead protocol
                      */
                     if (job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("시민")
                           || job[victim_index].substring(0, job[victim_index].indexOf(" ")).equals("경찰")) {
                        sendToallclient("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")) + "입니다.");
                     } else {
                        sendToallclient("D_START" + user[victim_index] + "가(이) 죽었습니다. 그의 직업은"
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")) + "입니다.");
                     }

                     kicked[victim_index] = true;
                     current_client--;
                     objectPerson();
                     if (objectCount == max_object) {
                        sendToallclient("T_START" + "all object selected");
                     } else {
                        sendToallclient("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                              + user[canSelect[2]]);
                     }

                  }

                  /*
                   * case when doctor choose user to save
                   */
                  else {
                     /*
                      * broadcast to every users that doctor saved the victim
                      */
                     sendToallclient("D_START" + "지난 밤, 의사는 마피아로부터 표적을 보호하는데 성공했습니다.");

                     objectPerson(); // choose user to click object randomly

                     if (objectCount == max_object) {
                        sendToallclient("T_START" + "all object selected");
                     } else {
                        sendToallclient("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                              + user[canSelect[2]]);
                     }
                  }
               }

               /*
                * case when user clicked the object
                */
               else if (game_start_flag == 1 && input.startsWith("object_clicked")) {
                  int msg_index = Integer.parseInt(input.substring(14));

                  /*
                   * if the username who clicked the object and the username allowed to choose is same
                   */
                  if (name.equals(user[canSelect[clickedNum]])) {

                     /*
                      * if the object is not picked (object_flag[msg_index] == true ) and current user didn't choose other object
                      * (isClicked[canSelect[clickedNum]] == false)
                      */
                     if (object_flag[msg_index] == true && isClicked[canSelect[clickedNum]] == false) {
                        PrintWriter sendObject = info.get(name);

                        /*
                         * if first and second users picked object
                         */
                        if (clickedNum != selectNum - 1) {

                           /*
                            * broadcast to every users that user picked an object and tell next user to pick object
                            */
                           for (PrintWriter writer : writers) {

                              /*
                               * if there are remaining object to be picked
                               */
                              if (objectCount != max_object - 1) {
                                 writer.println("FOUND" + user[canSelect[clickedNum]] + ","
                                       + user[canSelect[clickedNum + 1]]);
                              }
                              /*
                               * if all objects are picked
                               */
                              else {
                                 writer.println(
                                       "FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                              }

                           }
                           sendObject.println("object_description" + object_msg[msg_index]); 
                                                                              
                           object_flag[msg_index] = false; 
                           clickedNum++;  

                        }

                        /*
                         * if last user picks object
                         */
                        else {
                           sendToallclient("FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                           sendObject.println("object_description" + object_msg[msg_index]);
                           object_flag[msg_index] = false;
                           clickedNum++;
                        }
                        objectCount++;

                        /*
                         * if all of the object is picked
                         */
                        if (objectCount == max_object) {
                           sendToallclient("T_START" + "all object selected");
                        }
                     }

                     /*
                      * if other user already picked object -> send message other user already picked and tell to pick another object
                      */
                     else if (object_flag[msg_index] == false && isClicked[canSelect[clickedNum]] == false) {
                        PrintWriter sendObject = info.get(name);
                        sendObject.println("object_description" + "이미 다른 유저가 살펴 본 단서입니다.");
                     }
                  }

                  /*
                   * if is not the correct user's turn to pick object
                   */
                  else {
                     PrintWriter sendObject = info.get(name);
                     sendObject.println("object_description" + "아직 당신의 수색 차례가 아닙니다.");
                  }

                  /*
                   * if all of 3 users picked objects
                   */
                  if (clickedNum == selectNum) {

                     /*
                      * send every user timer_start protocol
                      */
                     sendToallclient("T_START");
                     clickedNum = 0;

                     /*
                      * initalize clicked num again -> because of next turn
                      */
                     for (int i = 0; i < selectNum; i++) {
                        canSelect[i] = 9999;
                     }
                  }

               }
               else {
                  for (PrintWriter writer : writers) {
                     if (!input.equals("")) 
                        writer.println("MESSAGE " + name + ": " + input);
                  }
               }

               /*
                * if every user finished voting
                */
               if (is_vote == current_client) {
                  int count = 0;
                  int temp_index = 0;
                  int same = 0;

                  /*
                   * find user who got the most vote 
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (vote[i] > count) {
                        count = vote[i];
                        temp_index = i;
                     }
                  }

                  /*
                   * check one more time if there are tie vote num
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (count == vote[i] && i != temp_index)
                        same = 1;
                  }

                  if (count == 0)
                     same = 0;

                  /*
                   * if there are no tie vote value
                   */
                  if (same != 1) {
                     PrintWriter victim = info.get(user[temp_index]);
                     victim.println("KICKED" + user_map_job);
                     sendToallclient("V_END" + user[temp_index] + "가(이) 처형됐습니다. 그의 직업은"
                           + job[temp_index].substring(0, job[temp_index].indexOf(" ")) + "입니다.");
                     kicked[temp_index] = true;
                     current_client--;
                  }

                  /*
                   * if there is tie in voting
                   */
                  else {
                     sendToallclient("V_END" + "투표에서 아무도 처형당하지 않았습니다.");
                  }
                  is_vote = 0;
                  count = 0;

                  /*
                   * initalize for next vote
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