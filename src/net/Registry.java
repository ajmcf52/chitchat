package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import misc.Constants;
import misc.ValidateInput;
import messages.NewUserMessage;
import messages.SimpleMessage;
import messages.ExitRoomMessage;
import messages.JoinRoomMessage;
import messages.ListRoomsMessage;
import messages.Message;
import messages.NewRoomMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class acts as one of the central units of processing within the Chatter
 * app. All requests are internally handled by the RequestHandler.
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
     * Map of data array objects representing all the current chat rooms that are
     * open and available to be joined. Entries are keyed on room names, which are
     * unique.
     */
    private static HashMap<String, String[]> roomListArrayMap;
    // Same as above, but in single-string CSV format. (for ease of sending across
    // the net)
    private static HashMap<String, String> roomListCsvMap;
    // map for tracking users in each of the rooms.
    private static HashMap<String, HashSet<String>> roomUsers;
    // for race conditions in accessing hashmaps above.
    private static Object roomListDataLock = new Object();

    private static HashMap<String, SessionCoordinator> coordinators; // threadpool of coordinators (key -> room name)

    public static void main(String[] args) {
        // initialize room data list
        roomListArrayMap = new HashMap<String, String[]>();
        roomListCsvMap = new HashMap<String, String>();
        roomUsers = new HashMap<String, HashSet<String>>();
        coordinators = new HashMap<String, SessionCoordinator>();

        try {
            // System.out.println("UCL --> " + userCountLock.toString());
            Socket socket; // socket variable for accepted connections.
            ServerSocket serverSocket = new ServerSocket(Constants.REGISTRY_PORT); // the server socket; accepts
                                                                                   // connections.

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
     * the purpose of this thread-based class is to be instantiated whenever the
     * Registry receives a new incoming connection. the resulting Socket for that
     * connection is passed off to this RequestHandler.
     * 
     * The nature of the desired request (determined by the first message received
     * via the Socket) will determine the RequestHandler's course of action.
     * 
     * I) For NewUserRequests, the current course of action is to send back a UID
     * for said user. Down the line, the Registry will keep static storage of all
     * users logged in to Chatter.
     */
    private static class RequestHandler extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        /**
         * constructor.
         * 
         * @param sock - connection socket to be used for servicing a request.
         */
        public RequestHandler(Socket sock) {
            socket = sock;
        }

        /**
         * message handler for NewUserMessages.
         * 
         * @param msg NewUserMessage
         */
        public void handleMessage(NewUserMessage msg) {
            String alias = msg.getAssociatedSenderAlias();
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
         * 
         * @param msg NewRoomMessage
         */
        public void handleMessage(NewRoomMessage msg) {
            String hostAlias = msg.getHost();
            String roomName = msg.getRoomName();
            String participantCountStr = "1";

            // determining the session port.
            // we lock, as other rooms could be being created simultaneously (race
            // condition)
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
            String[] roomListValues = { roomName, hostAlias, participantCountStr, sessionInfoContent };
            String roomListCsv = "";
            for (String s : roomListValues)
                roomListCsv += s + ",";
            roomListCsv = roomListCsv.substring(0, roomListCsv.length() - 1); // trim off the last ","
            HashSet<String> roomUserSet = new HashSet<String>();
            roomUserSet.add(hostAlias);

            // putting away the "book keeping" data
            synchronized (roomListDataLock) {
                roomListArrayMap.put(roomName, roomListValues);
                roomListCsvMap.put(roomName, roomListCsv);
                roomUsers.put(roomName, roomUserSet);
            }

            // send back a SimpleMessage containing the session connect information (ip and
            // port number).
            String content = "OK; ConnectInfo is " + sessionInfoContent; // NOTE this message format will be used
                                                                         // User-side.
            SimpleMessage response = new SimpleMessage(hostAlias, content);

            try {
                out.writeObject(response);
                out.flush();
                socket.close(); // NOTE this closes both associated streams as well.
            } catch (Exception e) {
                System.out.println("handleMessage(NRM) error --> " + e.getMessage());
            }
            sessionCount++;
        }

        /**
         * message handler function for LRMs.
         * 
         * @param msg the ListRoomsMessage to handle
         */
        public void handleMessage(ListRoomsMessage msg) {
            ArrayList<String> listings = null;
            synchronized (roomListDataLock) {
                listings = new ArrayList<String>(roomListCsvMap.values());
            }
            msg.setListings(listings);
            try {
                out.writeObject(msg);
                out.flush();
                socket.close(); // NOTE this closes both associated streams as well.
            } catch (Exception e) {
                System.out.println("handleMessage(LRM) error --> " + e.getMessage());
            }
        }

        /**
         * message handler for JRMs.
         * 
         * @param msg the JoinRoomMessage to be handled
         * 
         *                NOTE JRMs are actually initially sent to SessionCoordinators;
         *                they are simply forwarded to the Registry for the sake of book
         *                keeping.
         */
        public void handleMessage(JoinRoomMessage msg) throws NumberFormatException {
            String roomName = msg.getRoom();
            String alias = msg.getUserJoining();
            int participantCount = -1;
            synchronized (roomListDataLock) {

                // update room listing array (incrementing participant count by 1)
                String[] roomListingArray = roomListArrayMap.get(roomName);
                participantCount = Integer.parseInt(roomListingArray[Constants.GUEST_COUNT_TABLE_COLUMN]);
                participantCount++;
                roomListingArray[Constants.GUEST_COUNT_TABLE_COLUMN] = Integer.toString(participantCount);
                roomListArrayMap.put(roomName, roomListingArray);

                // update room listing CSV (simply using the array we just updated to re-format
                // CSV version)
                String roomListingCsv = "";
                for (String arg : roomListingArray)
                    roomListingCsv += arg;
                roomListingCsv = roomListingCsv.substring(0, roomListingCsv.length() - 1); // remove last ","
                roomListCsvMap.put(roomName, roomListingCsv);

                // update set of room users
                roomUsers.get(roomName).add(alias);
            }

            // build and write the SimpleMessage response.
            String responseContent = "OK; " + participantCount + " users now chatting.";
            SimpleMessage response = new SimpleMessage(alias, responseContent);

            try {
                out.writeObject(response);
                out.flush();
                socket.close(); // NOTE this closes both associated streams as well.
            } catch (Exception e) {
                System.out.println("handleMessage(JRM) error --> " + e.getMessage());
            }
        }

        /**
         * Handler method for catering to ExitRoomMessages. Note that these messages are
         * sent by ExitRoomWorkers to SessionCoordinators, who then forward them here.
         * ERMs are forwarded to the Registry for the sake of book-keeping.
         * 
         * @param msg ERM to be handled
         * @throws NumberFormatException
         */
        public void handleMessage(ExitRoomMessage msg) throws NumberFormatException {
            String alias = msg.getExitingUser();
            String roomName = msg.getAssociatedRoom();
            int participantCount = -1;

            synchronized (roomListDataLock) {
                String[] roomListingArray = roomListArrayMap.get(roomName);
                participantCount = Integer.parseInt(roomListingArray[Constants.GUEST_COUNT_TABLE_COLUMN]);
                participantCount--;
                if (participantCount == 0) {
                    roomListArrayMap.remove(roomName);
                    roomListCsvMap.remove(roomName);
                    roomUsers.remove(roomName);
                } else {
                    roomListingArray[Constants.GUEST_COUNT_TABLE_COLUMN] = Integer.toString(participantCount);
                    String csvString = "";
                    for (String arg : roomListingArray) {
                        csvString += arg + ",";
                    }
                    csvString = csvString.substring(0, csvString.length() - 1);
                    roomListCsvMap.put(roomName, csvString);
                    roomUsers.get(roomName).remove(alias);
                }

                String responseContent = "OK; " + participantCount + " users now chatting.";
                SimpleMessage response = new SimpleMessage(alias, responseContent);

                try {
                    out.writeObject(response);
                    out.flush();
                    socket.close(); // NOTE this closes both associated streams as well.
                } catch (Exception e) {
                    System.out.println("handleMessage(JRM) error --> " + e.getMessage());
                }
            }
        }

        public void run() {
            // handle the request!
            try {
                // NOTE order of constructor calls is crucial here! Reference ChatUser.java for
                // more details.
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                Object obj = in.readObject();
                Message msg = ValidateInput.validateMessage(obj);

                /**
                 * NOTE this style of programming is obviously far from ideal and violates DRY.
                 * 
                 * That said, I made a point of wanting to finish this project in 2 months, and
                 * so I am sacrificing some code quality here so I can push to get things done
                 * on time.
                 */
                if (msg instanceof NewUserMessage) {
                    handleMessage((NewUserMessage) msg);
                } else if (msg instanceof NewRoomMessage) {
                    handleMessage((NewRoomMessage) msg);
                } else if (msg instanceof ListRoomsMessage) {
                    handleMessage((ListRoomsMessage) msg);
                } else if (msg instanceof JoinRoomMessage) {
                    handleMessage((JoinRoomMessage) msg);
                } else if (msg instanceof ExitRoomMessage) {
                    handleMessage((ExitRoomMessage) msg);
                } else {
                    System.out.println("Unexpected Object Type Received by RequestHandler.. That's not good.");
                }

            } catch (Exception e) {
                System.out.println("RequestHandler Error! -->" + e.getMessage());
            }
        }
    }
}