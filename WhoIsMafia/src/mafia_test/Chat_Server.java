package mafia_test;

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
      private static final int PORT = 9001;
      
      private static HashSet<String> names = new HashSet<String>();
      private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
      private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>();
      private static int max_client = 7;
      private static int max_object = 10;
      private static int client_count = 0;

      private static int timer_flag = 0;
      private static int clickedNum = 0;
      private static int current_client = max_client; 
      
      private static int is_vote = 0;
      private static int mafia_index = 0;
      private static int police_index = 0;
      private static int doctor_index = 0;
      private static int victim_index = 0;
      private static int[] temp = new int[] { -1, -1, -1 };

      private static boolean[] selectedByMafia = new boolean[max_client];
      private static boolean[] kicked = new boolean[max_client];

      private static int[] vote = new int[max_client];

      private static String[] user = new String[max_client];
      private static PrintWriter[] ID = new PrintWriter[max_client];
      private static String story = "Eight people went to the villa for vacation./"
            + "Then two of them fought and one person died./"
            + "You have to find the clue and find the culprit by voting.";
       private static String totalJob = "";
      private static String[] job = { "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요! 마피아를 검거하면 당신들의 승리입니다.", 
    		  "의사 밤에 마피아에게 죽임을 당할 것 같은 시민을 살리세요! 시민이 모두 죽으면 마피아가 승리할 것 입니다. 단, 당신의 직업을 드러내도, 드러내지 않아도 됩니다.",
    		  "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요! 마피아를 검거하면 당신들의 승리입니다.", 
    		  "마피아 마피아는 단서에 쓰여진 당신의 정보를 숨겨야합니다. 정보가 드러나지 않게 주의하세요.", 
    		  "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요! 마피아를 검거하면 당신들의 승리입니다.",
    		  "경찰 밤에 당신이 알고싶은 플레이어의 직업을 알아볼 수 있습니다. 시민이 승리할 수 있도록 토론을 이끌어보세요.",
              "시민 단서와 토론을 통해 한 명의 마피아를 찾아내세요! 마피아를 검거하면 당신들의 승리입니다." };

      private static int[] random = { -1, -1, -1, -1, -1, -1, -1 };

      private static int[] footSize = { 245, 250, 255, 260, 265, 270, 275 };
      private static int selectNum = 3;
      private static int objectCount = 0;
      private static int[] canSelect = new int[selectNum];
      private static boolean[] isClicked = new boolean[max_client];
      private static boolean[] object_flag = new boolean[max_object];
      private static String[] object_msg = { user[police_index] + "의 품에서 경찰 뱃지를 보았다.", 
            user[doctor_index] + "의 목에는 병원 출입증이 걸려있다.", "발자국이 방 1로 이어져 있습니다.", "마피아 방에 대한 정보", "마피아가 없는 방에 대한 정보",
            "전체 발사이즈", "마피아 발사이즈", "오늘 야식으로 라면이 나온다고 한다", "텀프로젝트가 너무 많다", "어제 져넉은 맛있었다." };

      
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
         int client_value = (int) (Math.random() * max_client); // assign a random variable from 0 to 6 into client_value
         int cnt = 0; //if cnt is 0, there is no duplicated variable.

         for (int i = 0; i < selectNum; i++) { //for 3 players,
            if (client_value == canSelect[i]) //check between canSelct-array and client_value
               cnt++; //if there is same number, cnt++.  
         }

         if (cnt == 0 && (kicked[client_value] == false)) { //if there is no duplication and he is not kicked,
            canSelect[count] = client_value; //he can pick object.
            count++; //for next player until 3 players.
         }

         if (count == selectNum) //if 3 players pick each object,
            break; //out.
      }
   }

   
   /**********************************************************************************************************
    * void initialize
    * 
    * intialize the variables.
 *************************************************************************************************************/
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

      
       //the information about story when player clicks job button
      totalJob = "," + job[0].substring(0, job[0].indexOf(" ")) + "," + job[0].substring(job[0].indexOf(" ") + 1)
            + "," + job[1].substring(0, job[1].indexOf(" ")) + "," + job[1].substring(job[1].indexOf(" ") + 1) + ","
            + job[3].substring(0, job[3].indexOf(" ")) + "," + job[3].substring(job[3].indexOf(" ") + 1) + ","
            + job[5].substring(0, job[5].indexOf(" ")) + "," + job[5].substring(job[5].indexOf(" ") + 1);
   }

   
   /**********************************************************************************************************
    * void randomArray
    * 
    * store random value into random array
    * ex : 1 5 3 6 2 4 0
    * 
    * @index[integer]: index number
    * @client_value[integer]: random number
    * @cnt[int]: checking for duplication
    **********************************************************************************************************/
   private static void randomArray() {
      int index = 0;
      
      while (true) {
         int client_value = (int) (Math.random() * max_client);
         int cnt = 0;
         for (int i = 0; i < max_client; i++) { //assign a ramdom number to each players
            if (client_value == random[i])
               cnt++;
         }
         if (cnt == 0) { //if there is no duplication
            random[index] = client_value; //store it
            index++;
         }
         if (index == max_client)//do until 7 players get random index
            break;
      }
   }

   
   /**********************************************************************************************************
    * void storeIndex
    * 
    * store index who is Mafia, police or doctor.
    **********************************************************************************************************/
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

   
   /**********************************************************************************************************
    * void assignClue_pol_doc
    * 
    * assign a string of each player into object
    **********************************************************************************************************/
   private static void assignClue_pol_doc() {
      object_msg[0] = user[police_index] + "의 품에서 경찰 뱃지를 보았다.";
      object_msg[1] = user[doctor_index] + "의 목에는 병원 출입증이 걸려있다.";
   }

   
   /**********************************************************************************************************
    * void assignClue_room1
    * 
    * names in room where Mafia is staying.
    * 
    * @index[integer]: index number
    * @client_value[integer]: random number
    * @cnt[int]: checking for duplication
    **********************************************************************************************************/
   private static void assignClue_room1() {
      object_msg[3] = "room1," + user[mafia_index]; //update the Mafia index to object_msg.
      int index = 0;

      while (true) {
         int client_value = (int) (Math.random() * max_client); //assign a random number into client_value
         int cnt = 0;
         
         if (client_value != mafia_index) { //if the index is not same with mafia_index,
            for (int i = 0; i < 3; i++) {
               if ((client_value == temp[i])) //store it into temp
                  cnt++; 
            }
            if (cnt == 0) { //if it is duplicate index,
               temp[index] = client_value; //store it into temp
               object_msg[3] += "," + user[client_value]; //and they are people who is staying room1 with Mafia
               index++;
            }
            if (index == 3) //if 3people gather, break.
               break;
         }
      }
   }

   
   /**********************************************************************************************************
    * void assignClue_room2
    * 
    * names in room where Mafia is not staying.
    * 
    * @index[integer]: index number
    * @client_value[integer]: random number
    * @cnt[int]: checking for duplication
    **********************************************************************************************************/
   private static void assignClue_room2() {
      object_msg[4] = "room2";
      int index = 0;
      int[] temp2 = new int[] { -1, -1, -1 };
      
      while (true) {
         int client_value = (int) (Math.random() * max_client);
         int cnt = 0;
         
         if (client_value != mafia_index) { //if the index is not same with mafia index,
            for (int i = 0; i < 3; i++) {
               if ((client_value == temp[i] || client_value == temp2[i])) //and it is not in room1,
                  cnt++; //check
            }
            if (cnt == 0) { //if there is no duplication,
               temp2[index] = client_value; //store it into temp2
               object_msg[4] += "," + user[client_value];
               index++;
            }
            if (index == 3) //after gathering 3 people,
               break; //break.
         }
      }
   }

   
   /**********************************************************************************************************
    * void assignClue_totalFootSize
    * 
    *  informations about all player's foot size.
    **********************************************************************************************************/
   private static void assignClue_totalFootSize() {
      object_msg[5] = "foot size," + user[0] + " : " + footSize[0] + "," + user[1] + " : " + footSize[1] + ","
            + user[2] + " : " + footSize[2] + "," + user[3] + " : " + footSize[3] + "," + user[4] + " : "
            + footSize[4] + "," + user[5] + " : " + footSize[5] + "," + user[6] + " : " + footSize[6];
   }

   
   /**********************************************************************************************************
    * void assignClue_mafiaFoorSize
    * 
    *  informations about Mafia's foot size.
    **********************************************************************************************************/
   private static void assignClue_mafiaFootSize() {
      object_msg[6] = "mafia foot size," + (footSize[mafia_index] - 5) + " ~ " + (footSize[mafia_index] + 5);
   }

   
   /**********************************************************************************************************
    * void main
    * 
    * run a server program
    * after assgining index randomly and initialize about them,
    * do handler.
    **********************************************************************************************************/
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
   
   
   /**********************************************************************************************************
    * class Handler
    * 
    * this is for socket communication
    * 
    * @name[String]: user's name
    * @socket[Socket]: socket
    * @in[BufferedReader]: read buffer
    * @out[printWriter]: print out
    **********************************************************************************************************/
   private static class Handler extends Thread {

      private String name;
      private Socket socket;
      private BufferedReader in;
      private PrintWriter out;

      public Handler(Socket socket) { //make socket
         this.socket = socket;
      }

      /**********************************************************************************************************
       * class run
       * 
       * this is for socket communication about sending and receiving
       **********************************************************************************************************/
      public void run() {
         try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            /*send protocol to client*/
            while (true) {
               out.println("SUBMITNAME"); //submit name by client
               name = in.readLine();
               if (name == null) {
                  return;
               }
               synchronized (names) { //synchronize alerting names to all players now
                  sendToallclient("CONNECT " + name + " is connected.\n");

                  if (!names.contains(name)) { //no duplicate name
                     names.add(name);

                     for (PrintWriter writer : writers) { //message send about who is entering now
                        writer.println("MESSAGE " + "[" + name + "] enter");
                     }
                     break;
                  }
               }
            }
            out.println("NAMEACCEPTED"); //after accepting name, alert it.

            writers.add(out);
            user[client_count] = name; //assign a name into array
            ID[client_count] = out; //assign an address into array

            client_count++; //for next entering

            System.out.println(user[client_count - 1] + "님이 입장하셨습니다.");
            System.out.println("현재 인원 " + client_count + "명");

            info.put(name, out);
            for (int i = 0; i < selectNum; i++)
               System.out.println(canSelect[i]);
            if (client_count == max_client) {
               objectPerson();
               for (PrintWriter writer : writers) { //message send about starting
                  writer.println("MESSAGE " + "game start");
               }
               for (PrintWriter writer : writers) { //messsage send about who can pick object
                  writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                        + user[canSelect[2]]);
               }

               for (int i = 0; i < max_client; i++) { //assing a job to each players
                  String temp = job[i];
                  job[i] = job[random[i]];
                  job[random[i]] = temp;
               }

               storeIndex();
               assignClue_pol_doc(); //assign informations into each objects
               assignClue_room1();
               assignClue_room2();
               assignClue_totalFootSize();
               assignClue_mafiaFootSize();
            }

            while (true) {//calculate results
               if (kicked[mafia_index] == true) { //if mafia is kicked,
                  for (PrintWriter writer : writers) {
                     writer.println("MESSAGE " + "mafia dead!, citizen win!"); //alert the result to all clients.
                  }
                  System.exit(0); //exit the program.
               } else if (current_client == 2) { //if the remaining player is 2 people,
                  for (PrintWriter writer : writers) { //it is the mafia's win.
                     writer.println("MESSAGE " + "mafia win!"); //alert the result.
                  }
                  System.exit(0);
               }
               String input = in.readLine();
               if (input == null) {
                  return;
               }
 
               
               
               /*receive protocol from client*/
               //if user clicked job button and client send '/job' protocol in RoomGUI,
               else if (input.startsWith("/") && input.indexOf("job") != -1) {
                  int temp_index = 0;

                  for (int i = 0; i < client_count; i++) {
                     if (name.equals(user[i]))
                        temp_index = i;
                  }

                  PrintWriter sender = info.get(name);
                  sender.println("SHOW_JOB" + job[temp_index]); //alert his job
               }

           
              //if user clicked story button and client send '/story' protocol in RoomGUI,
               else if (input.startsWith("/") && input.indexOf("story") != -1) {
                  int temp_index = 0;

                  for (int i = 0; i < client_count; i++) {
                     if (name.equals(user[i]))
                        temp_index = i;
                  }

                  PrintWriter sender = info.get(name);
                  sender.println("SHOW_STORY" + story + totalJob); //alert a story and jobs' role
               }

               //after one client's timer is over,
               else if (input.startsWith("/") && input.indexOf("timeout") != -1) {
                  timer_flag++;
                  System.out.println(timer_flag);
                  
                  if (timer_flag == current_client) { //if all player's timer is over,
                     String temp = null;
                     for (int i = 0; i < max_client; i++) {
                        if (kicked[i] == false) { //sorting array of player
                           if (temp == null) {
                              temp = user[i];
                           } else {
                              temp += ("," + user[i]);
                           }
                        }
                     }
                     System.out.println(temp);
                    
                     for (PrintWriter writer : writers) {
                        writer.println("VOTENAME " + temp); //send a protocol, 'VOTENAME' to send list about player's name to be voted.
                     }
                     timer_flag = 0; // initialize 0 for next voting
                  }
               }

              
               //if client send voted-name by protocol '/victim',
               else if (input.startsWith("/") && input.indexOf("victim") != -1) {
                  String victim = input.substring(7);
                  int temp_index = 0;
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(victim) && kicked[i] == false)
                        temp_index = i; //store voted number about each player
                  }
                  is_vote++;
                  vote[temp_index]++;
               }

          
               //at night, make police to do his job by protocol, "/police"
               else if (input.startsWith("/") && input.indexOf("police") != -1) {
                  PrintWriter police = info.get(name);
                  String temp = null;

                  //storing array of player's name except for police
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

                  //and if police had not be kicked,
                  if (kicked[police_index] == false) {
                	  //if it is same between police's name and user's name who sends '/police'protocol,
                     if (name.equals(user[police_index]))
                        police.println("JOB" + temp); //send array of user's names by '/job' protocol.
                  }
                  
                 //but police had be kicked,
                  else {
                	  //send 'non' protocol to mafia
                     if (name.equals(user[mafia_index]))
                        police.println("NON");
                  }
               }
               
              
               //if the polcie picked person whom he wants to know his job,
               else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
                  PrintWriter police = info.get(user[police_index]);
                  String selected = input.substring(13);
                  int temp_index = 9999;
                  System.out.println("selected : " + selected); //receive selected person's name
                  
                  
                  //extract index whose name is matches with police's voting
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        temp_index = i;
                     System.out.println("user[" + i + "] : " + user[i]);
                  }
                  
              
                  //send his job to police by using protocol '/IS_MAFIA'
                  police.println("IS_MAFIA?" + user[temp_index] + "' job is "
                        + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
               }
               
               
               //if server receive '/kill' protocol from mafia,
               else if (input.startsWith("/") && input.indexOf("kill") != -1) {
                  PrintWriter mafia = info.get(user[mafia_index]); //store mafis's address
                  String temp = null;

                  //store name into array except to mafia and kicked-people.
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

                  //send '/kill' protocol to mafia
                  mafia.println("KILL" + temp);
                  System.out.println("마피아 명단 넘어감");

               }

               //if mafia picked kicked-person,
               else if (input.startsWith("/") && input.indexOf("dead") != -1) {
                  String selected = input.substring(5); // extract a name who is picked called 'victim'
                  PrintWriter dead = info.get(selected);
                  
                  //extract index picked by mafia
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        victim_index = i;
                  }

                  
                  //if the doctor is not kicked,
                  if (kicked[doctor_index] == false) {
                     PrintWriter doctor = info.get(user[doctor_index]);
                     selectedByMafia[victim_index] = true;

                     String temp = null;
                    
                     //store array of name including his name but except to kicked-person
                     for (int i = 0; i < max_client; i++) {
                        if (kicked[i] == false) {
                           if (temp == null) {
                              temp = user[i];
                           } else {
                              temp += ("," + user[i]);
                           }
                        }
                     }
                     
                     doctor.println("DOCTOR" + temp); //send them to doctor by protocol '/doctor'
                  }

                
                  else { //if the doctor had kicked,
                	  // send protocol 'KICKED' which is about kicking to picked-player pick by Mafia.
                     dead.println("KICKED");

                     //broadcast about who was dead, his name and job.
                     // and send protocol, 'D_STARt', start new day.
                     for (PrintWriter writer : writers) {
                        writer.println("D_START" + user[victim_index] + " dead, he was "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")));
                     }
                     kicked[victim_index] = true; // kick user who is picked from game
                     current_client--;
                     objectPerson(); //select people who can pick object, randomly.

                    
                     //if all users clicked objects,
                     if (objectCount == max_object) {
                    	 //send protocol T_START to timer setting.
                        for (PrintWriter writer : writers) {
                           writer.println("T_START" + "all object selected");
                        }
                     }

                     //if there is object to pick, yet,
                     else {
                    	 //alert all users about names who can pick object by protocol, "CLUEFINDER"
                        for (PrintWriter writer : writers) {
                           writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                                 + user[canSelect[2]]);
                        }

                     }

                  }
               }


                  //if doctor picked saving-person,
               else if (input.startsWith("/") && input.indexOf("protect") != -1) {
                  PrintWriter dead = info.get(user[victim_index]);
                  int temp_index = 9999;
                  String protect = input.substring(8);

                  //extract index of doctor's pick
                  for (int i = 0; i < max_client; i++) {
                     if (protect.equals(user[i]))
                        temp_index = i;
                  }

                  selectedByMafia[temp_index] = false;//cancel information about Mafia's pick
                  //so if false person can be made false, called victim,
                  //that is the case doctor cannot save him.


                  //if doctor did not pick Mafia's pick,
                  if (selectedByMafia[victim_index] == true) {
                     dead.println("KICKED"); //kick victim

                     //alert to all clients.
                     for (PrintWriter writer : writers) {
                        writer.println("D_START" + user[victim_index] + " dead, he was "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")));
                     }
                     kicked[victim_index] = true;
                     current_client--;
                     objectPerson(); //start a day again.
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

                  //if doctor picked Maifa's pick,
                  else {
                     for (PrintWriter writer : writers) { //broadcast to all clients that doctor save victim
                        writer.println("D_START" + "Doctor saved victim");
                     }

                     objectPerson(); //pick people who can pick object

                     if (objectCount == max_object) {
                        for (PrintWriter writer : writers) {
                           writer.println("T_START" + "all object selected"); //timer start
                        }
                     } else {
                        for (PrintWriter writer : writers) {
                           writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                                 + user[canSelect[2]]);
                        }
                     }
                  }
               }

               else if (input.startsWith("object_clicked")) { //if user clicked object,
                  int msg_index = Integer.parseInt(input.substring(14));
                  
                  //if user's name is same with object-picked persons's name,
                  if (name.equals(user[canSelect[clickedNum]])) {
                     
                      //and if object had not picked and the other player didn't pick it,
                     if (object_flag[msg_index] == true && (isClicked[canSelect[clickedNum]] == false)) {
                        PrintWriter sendObject = info.get(name);

                        System.out.println(msg_index);

                        if (clickedNum != selectNum - 1) { //if the first and second user clicked object,
                           
                        	//send information about that the users picked object and alert the next user have to pick.
                           for (PrintWriter writer : writers) {
                              if (objectCount != max_object - 1) {//if there are non-picked object,
                                 writer.println("FOUND" + user[canSelect[clickedNum]] + ","
                                       + user[canSelect[clickedNum + 1]]);
                              } 

                              else { //if there is no remaining object to pick, (10 objects was found already)
                                 writer.println(
                                       "FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                              }

                           }
                           sendObject.println("object_description" + object_msg[msg_index]); // send message about picked object
                           object_flag[msg_index] = false; // and make object to be inactive
                           clickedNum++;
                        } 
                        
                        
                        else { //if the last user clicks object,
                           for (PrintWriter writer : writers) {
                              writer.println("FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                           }
                           sendObject.println("object_description" + object_msg[msg_index]);
                           object_flag[msg_index] = false;
                           clickedNum++;
                        }
                        objectCount++;
                        
                      //if there is no remaining object to pick, (10 objects was found already)
                        if (objectCount == max_object) {
                           for (PrintWriter writer : writers) {
                              writer.println("T_START" + "all object selected");
                           }
                        }
                     } 
                     
                     //if the other player picked object already, user should pick another object.
                     else if (object_flag[msg_index] == false && isClicked[canSelect[clickedNum]] == false) {
                        PrintWriter sendObject = info.get(name);
                        sendObject.println("object_description" + "이미 다른 사람이 선택한 오브젝트입니다.");
                     }
                  } 
                  
                  else { //if the turn is not for user,
                     PrintWriter sendObject = info.get(name);
                     sendObject.println("object_description" + "당신의 차례가 아닙니다");
                  }


                  if (clickedNum == selectNum) { //after 3 picked-players pick objects,
                     
                	  //send protocol to timer start
                     for (PrintWriter writer : writers) {
                        writer.println("T_START");
                     }
                     clickedNum = 0;
                     

                     //initialize again to select user who can pick object in next turn
                     for (int i = 0; i < selectNum; i++) {
                        canSelect[i] = 9999;
                     }
                  }

               } 
               
               else {//just for chatting protocol
                  for (PrintWriter writer : writers) {
                     if (!input.equals("")) //if there are no NULL when client send enter-key and no message,
                        writer.println("MESSAGE " + name + ": " + input); //show his message
                  }
               }

               
               if (is_vote == client_count) {  //after all users finish voting,
                  int count = 0;
                  int temp_index = 0;
                  int same = 0;
                  
                  //find out user who is voted by the highest number.
                  for (int i = 0; i < max_client; i++) {
                     if (vote[i] > count) {
                        count = vote[i];
                        temp_index = i;
                     }
                  }
                  
                  /*
                   * 한번 더 검사해서 동률이  있는지 찾아냄
                   */
                  //check whether there is tie or not
                  for (int i = 0; i < max_client; i++) {
                     if (count == vote[i] && i != temp_index)
                        same = 1;
                  }

                  if (count == 0) same = 0;
                     
                  //if there is no tie,
                  if (same != 1) {
                     PrintWriter victim = info.get(user[temp_index]);
                     victim.println("KICKED"); //kick.
                     for (PrintWriter writer : writers) {
                        writer.println("V_END" + user[temp_index] + " dead, he was "
                              + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
                     }
                     kicked[temp_index] = true;
                     current_client--;
                  } 
                  
                  //if there is tie,
                  else {
                     for (PrintWriter writer : writers) {
                        writer.println("V_END" + "Nothing happened"); //no kick.
                     }
                  }
                  
                  //initialize for next turn.
                  is_vote = 0;
                  count = 0;
                  
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
   
   
   
   /**********************************************************************************************************
    * void sendToallClient
    * 
    * send to all client about message
    **********************************************************************************************************/
   public static void sendToallclient(String mssg) {
      for (PrintWriter writer : writers) {
         writer.println(mssg);
         writer.flush();
      }
   }
}
