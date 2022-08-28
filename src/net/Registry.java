package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import misc.Constants;
import misc.ValidateInput;
import messages.NewUserMessage;
import messages.SimpleMessage;
import messages.Message;
import messages.NewRoomMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

/**
 * This class acts as one of the central units of processing within the Chatter app.
 * All requests are internally handled by the RequestHandler.
 * 
 * 
 */
public class Registry {
    private static volatile int userCount = 0;
    private static Object userCountLock = new Object();
    private static boolean running = false;
    private static volatile int sessionCount = 0;
    private static Object sessionCountLock = new Object();

    /**
     * Map of data array objects representing all the current
     * chat rooms that are open and available to be joined.
     * Entries are keyed on room names, which are unique.
     */
    private static HashMap<String,String[]> roomListArrayMap;
    // Same as above, but in single-string CSV format. (for ease of sending across the net)
    private static HashMap<String,String> roomListCsvMap;
    // map for tracking users in each of the rooms.
    private static HashMap<String, ArrayList<String>> roomUsers;
    // for race conditions in accessing hashmaps above.
    private static Object roomListDataLock = new Object(); 
    
    private static HashMap<String,SessionCoordinator> coordinators; // threadpool of coordinators (key -> room name)

    public static void main(String[] args) {
        // initialize room data list
        roomListArrayMap = new HashMap<String,String[]>();
        roomListCsvMap = new HashMap<String,String>();
        roomUsers = new HashMap<String, ArrayList<String>>();
        coordinators = new HashMap<String,SessionCoordinator>();

        try {
            //System.out.println("UCL --> " + userCountLock.toString());
            Socket socket; // socket variable for accepted connections.
            ServerSocket serverSocket = new ServerSocket(Constants.REGISTRY_PORT); // the server socket; accepts connections.

            System.out.println("Server Registry listening on port" + Constants.REGISTRY_PORT);
            running = true;
            while (running) {
                socket = serverSocket.accept();
                System.out.println("Connection received by Registry");
                RequestHandler handler = new RequestHandler(socket);
                handler.start();
                System.out.println("Connection passed off to RequestHandler");
            }
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            
        } catch (IOException e) {
            System.out.println("Registry IO Error!! --> " + e.getMessage());
        }
    }

    /**
     * the purpose of this thread-based class is to be instantiated whenever the Registry receives a new incoming connection.
     * the resulting Socket for that connection is passed off to this RequestHandler.
     * 
     * The nature of the desired request (determined by the first message received via the Socket) will determine the 
     * RequestHandler's course of action.
     * 
     * I) For NewUserRequests, the current course of action is to send back a UID for said user. 
     * Down the line, the Registry will keep static storage of all users logged in to Chatter.
     */
    private static class RequestHandler extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        /**
         * constructor.
         * @param sock - connection socket to be used for servicing a request.
         */
        public RequestHandler(Socket sock) {
            socket = sock;
        }

        /**
         * message handler for NewUserMessages.
         * @param msg NewUserMessage
         */
        public void handleMessage(NewUserMessage msg) {
            String alias = msg.getAlias();
            int uidNum = -1;

            synchronized (userCountLock) {
                userCount++;
                uidNum = userCount;
            }
            String uid = Constants.UID_PREFIX + String.valueOf(uidNum);
            String content = "OK; UID is " + uid;
            SimpleMessage response = new SimpleMessage(alias, content);
            try {
                out.writeObject(response);
                out.flush();
            } catch (Exception e) {
                System.out.println("RRH handleMessage(NUM) error --> " + e.getMessage());
            }
        }

        /**
         * message handler for NewRoomMessages.
         * @param msg NewRoomMessage
         */
        public void handleMessage(NewRoomMessage msg) {
            String hostAlias = msg.getHost();
            String roomName = msg.getRoomName();
            String participantCountStr = "1";

            // determining the session port.
            // we lock, as other rooms could be being created simultaneously (race condition)
            int sessionPort = -1;
            synchronized (sessionCountLock) {
                sessionPort = Constants.SESSION_PORT_PREFIX + sessionCount;
            }

            // booting up the SeshCoordinator thread.
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(sessionPort);
            } catch (Exception e) {
                System.out.println("handleMessage(NRM) error creating ServerSocket --> " + e.getMessage());
            }
            SessionCoordinator seshCoord = new SessionCoordinator(sessionCount, serverSocket, hostAlias, roomName);
            coordinators.put(roomName, seshCoord);
            seshCoord.start();

            // more info determination
            String ipString = serverSocket.getInetAddress().getHostAddress();
            String portStr = Integer.toString(sessionPort);
            String sessionInfoContent = ipString + ":" + portStr;

            // putting together some book keeping data values
            String[] roomListValues = {roomName, hostAlias, participantCountStr, sessionInfoContent};
            String roomListCsv = "";
            for (String s : roomListValues)
                roomListCsv += s + ",";
            roomListCsv = roomListCsv.substring(0,roomListCsv.length()-1); // trim off the last ","
            ArrayList<String> roomUserList = new ArrayList<String>();
            roomUserList.add(hostAlias);

            // putting away the "book keeping" data
            synchronized (roomListDataLock) {
                roomListArrayMap.put(roomName, roomListValues);
                roomListCsvMap.put(roomName, roomListCsv);
                roomUsers.put(roomName, roomUserList);
            }

            // send back a SimpleMessage containing the session connect information (ip and port number).
            String content = "OK; ConnectInfo is " + sessionInfoContent; // NOTE this message format will be used User-side.
            SimpleMessage response = new SimpleMessage(hostAlias, content);

            try {
                out.writeObject(response);
                out.flush();
                socket.close(); // NOTE this closes both associated streams as well.
            } catch (Exception e) {
                System.out.println("handleMessage(NRM) error --> " + e.getMessage());
            }
        }

        public void run() {
            // handle the request!
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                Object obj = in.readObject();
                Message msg = ValidateInput.validateMessage(obj);
                if (msg instanceof NewUserMessage) {
                    handleMessage((NewUserMessage)msg);
                }
                else if (msg instanceof NewRoomMessage) {
                    handleMessage((NewRoomMessage)msg);
                }


                //         // write back the inet address + port of the SC's server socket for Chat host to connect to
                //         String inetAddressStr = serverSocket.getInetAddress().toString();
                //         String portStr = String.valueOf(serverSocket.getLocalPort());
                //         String connectionInfoMsg = inetAddressStr + ":" + portStr + '\n';

                //         // update local static fields before responding.
                //         String[] roomListValues = {roomName, alias, participantCountStr, connectionInfoMsg};  // using SID for room name (For now)
                //         String roomListCsv = roomName + "," + alias + "," + participantCountStr + "," + connectionInfoMsg;
                //         ArrayList<String> roomUserList = new ArrayList<String>();
                //         roomUserList.add(alias);
                //         // plan is to add room naming capability once other functionalities are fleshed out.
                //         synchronized (roomListDataLock) {
                //             roomListArrayMap.put(roomName, roomListValues);
                //             roomListCsvMap.put(roomName,roomListCsv);
                //             roomUsers.put(roomName, roomUserList);
                //         }

                //         out.write(connectionInfoMsg);
                //         // stream work done.
                //         out.flush();
                //         out.close();
                //         in.close();
                //         socket.close();
                //         // work done, time to exit.
                //         break;
                //     }

                //     case (Requests.LIST_ROOMS_REQ): {
                //         /* we enter here if the incoming messages
                //          * pertains to a rooms list request.
                //          */
                //         RoomsListHandler rlh = new RoomsListHandler(socket);
                //         rlh.start();
                //         break;
                //     }

                //     case (Requests.JOIN_ROOM_REQ): {
                //         /** Enter here when a user is requesting to join an existing room.
                //          * NOTE perhaps illogically, because the connection info is readily available from
                //          * looking at the room listing in RoomSelectTable, Registry doesn't have to be contacted
                //          * first for SessionCoordinator's connection information.
                //          * 
                //          * Thus, SC contacts Registry post-haste for the sake of notifying that a new user has
                //          * joined their room.
                //          */
                        
                //         // alias of user joining and name of room being joined are sent delimited on the same line by ","
                //         String aliasAndRoomName = in.readLine();
                //         String[] args = aliasAndRoomName.split(",");
                //         String alias = args[0];
                //         String roomName = args[1];

                //         // lock before updating data structures
                //         synchronized (roomListDataLock) {
                //             String[] roomDataArr = roomListArrayMap.get(roomName);
                //             int count = Integer.parseInt(roomDataArr[Constants.GUEST_COUNT_TABLE_INDEX]);
                //             count++;
                //             roomDataArr[Constants.GUEST_COUNT_TABLE_INDEX] = Integer.toString(count);
                //             roomListArrayMap.put(roomName, roomDataArr);

                //             String roomDataCsv = "";
                //             for (String s : roomDataArr) {
                //                 roomDataCsv += s + ",";
                //             }
                //             roomDataCsv.substring(0, roomDataCsv.length() - 1);
                //             roomListCsvMap.put(roomName, roomDataCsv);

                //             roomUsers.get(roomName).add(alias);
                //         }
                //         out.write("OK\n");
                //         out.flush();
                //         out.close();
                //         in.close();
                //         socket.close();
                //         break;
                //     }
                    
                //     default: {
                //         System.out.println("RequestHandler -> default case.");
                //     }
                // }

            } catch (Exception e) {
                System.out.println("RequestHandler Error! -->" + e.getMessage());
            }
        }
    }

    /**
    * this class represents a persisent thread-based worker
    * that is spawned by the Registry to handle really two
    * types of requests: Room Listing requests, and 
    * Room Listing Refresh requests.

    * The difference in the second case is that, before sending
    * the list across the wire to the user, the RLH compares what
    * it sent last to what it's about to send, and it uses a simple,
    * mutually understood messaging protocol to effectively communicate
    * any additions or removals to the list that was last sent.

    The cool thing here, is that due to the nature of these two requests,
    we don't need a separate Reader/Writer workers. The handling will be the
    virtually the same in all cases:
        1. send over the rooms list
        2. sit and wait for a refresh request, and upon receiving one,
        respond accordingly
        3. repeat step 2 until "DONE" is received, or socket is closed. Easy peasy.
    */
    private static class RoomsListHandler extends Thread {
        private Socket socket;

        /**
         * constructor for RLH
         * @param sock socket to be used
         */
        public RoomsListHandler(Socket sock) {
            socket = sock;
        }

        /**
         * this thread's main line of execution.
         */
        public void run() {
            BufferedReader in;
            PrintWriter out;

            /**
                 * There is a non-trival performance trade-off here.
                 * 
                 * Option 1: Grab the lock, send the list over, release.
                 * 
                 * Option 2: Grab the lock, copy the list of rooms, 
                 * release, and send the list over.
                 * 
                 * Option 2 avoids a copy operation and will work better
                 * at a smaller scale. Option 1 will be more preferable
                 * for a larger user base.
                 * 
                 * Even then, however, will a larger user base, copying the
                 * list over every time becomes unrealistic. 
                 * 
                 * In a higher-scale situation with thousands or millions of users,
                 * it might make sense to have two lists, one of which is updated
                 * as rooms are created and deleted, the second being slightly behind,
                 * thus less accurate by a small margin of time, but more accessible.
                 * Some thread would be charged with the task of updating the 2nd list
                 * when it can, but for all intents and purposes, read operations could
                 * continue without the need for locking on the 2nd list (unless it was
                 * being updated). This would be a super cool problem to dig into further,
                 * as it seems it would be a relevant issue after simple contemplation.
                 */

            /**
             * start with sending over the current set of room data
             */
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                // working with Option 1 for now (from block comment above)

                sendRooms(out);
                out.flush();
                // wait for either a refresh request, a DONE response, or the socket to close
                String line;
            
                while (true) {
                    line = in.readLine();
                    // if we get "DONE" back (or not "REFRESH"), we can assume the user has joined a room.
                    if (line.equals("DONE") || !line.equals("REFRESH")) {
                        break;
                    }
                    // otherwise, we are to send the most up-to-date list of the rooms available.
                    sendRooms(out);
                }

            } catch (Exception e) {
                System.out.println("RLH Error!!! --> " + e.getMessage());
            }
        }

        /**
         * Method used to send over the most recent rooms list to a particular user.
         * This method could incur race conditions, hence why we synchronize.
         * @param out output stream being used to send the data.
         */
        public void sendRooms(PrintWriter out) {
            
            synchronized (roomListDataLock) {
                int numRooms = roomListCsvMap.size();
                out.write("BEGIN " + Integer.toString(numRooms) + '\n');
                for (String value : roomListCsvMap.values()) {
                    out.write(value + '\n'); // send comma-separated room data String values.
                }
                out.write("DONE\n");
            }
        }
    }
}