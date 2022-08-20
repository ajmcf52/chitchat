package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import misc.Constants;
import misc.Requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * This class is responsible for performing many of the miscellaneous tasks required for the Chatter application to work.
 * 
 * registry functions:
 * I) store user count, providing fresh user ID numbers (UIDs) for new users.
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
    // for race conditions in accessing hashmaps above.
    private static Object roomListDataLock = new Object(); 
    
    private static HashMap<String,SessionCoordinator> coordinators; // threadpool of coordinators (key -> room name)

    public static void main(String[] args) {
        // initialize room data list
        roomListArrayMap = new HashMap<String,String[]>();
        roomListCsvMap = new HashMap<String,String>();
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

        /**
         * constructor.
         * @param sock - connection socket to be used for servicing a request.
         */
        public RequestHandler(Socket sock) {
            socket = sock;
        }

        public void run() {
            // handle the request!
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                String requestString = in.readLine();

                switch (requestString) {
                    /*
                     * here, we are servicing a NewUserRequest. To fulfill this request at a basic level,
                     * all we have to do is send back a fresh UID for the user, incrementing userCount after doing so.
                     */
                    case (Requests.NEW_USER_REQ): {
                        String alias = in.readLine(); // ChatUser's alias. This can be saved in a HashMap at a later date.
                        int uidNum = -1;

                        // get a hold of userCount (synchronized access)
                        synchronized (userCountLock) {
                            // increment userCount, copy value into a placeholder, and exit sync block
                            userCount++;
                            uidNum = userCount;
                        }

                        String uid = Constants.UID_PREFIX + String.valueOf(uidNum); // generate UID string

                        // whisk the UID back over the socket.
                        out.write(uid);
                        // Work done! Prepare for exit.
                        out.flush();
                        out.close();
                        in.close();
                        break;
                    }
                    /**
                     * here we are servicing a new room request. To fulfill this request, we must spawn
                     * a SessionCoordinator and pass along a ServerSocket to which the host ChatUser will connect to.
                     */
                    case (Requests.NEW_ROOM_REQ): {
                        String alias = in.readLine();
                        String participantCountStr = "1"; // always the case when a room is first created (only the host!)

                        String sid = "";
                        int scPort = -1;
                        synchronized (sessionCountLock) {
                            // we sync in case two workers are trying to create a room at nearly the same moment in time.
                            sid = Constants.SID_PREFIX + String.valueOf(sessionCount);
                            scPort = Constants.SESSION_PORT_PREFIX + sessionCount;
                        }

                        ServerSocket serverSocket = new ServerSocket(scPort);
                        SessionCoordinator sc = new SessionCoordinator(sessionCount, serverSocket, alias, sid);
                        coordinators.put(sid, sc);
                        sc.start();

                        // write back the inet address + port of the SC's server socket for Chat host to connect to
                        String inetAddressStr = serverSocket.getInetAddress().toString();
                        String portStr = String.valueOf(serverSocket.getLocalPort());
                        String connectionInfoMsg = inetAddressStr + ":" + portStr + '\n';

                        // update local static fields before responding.
                        String[] roomListValues = {sid, alias, participantCountStr, connectionInfoMsg};  // using SID for room name (For now)
                        String roomListCsv = sid + "," + alias + "," + participantCountStr + "," + connectionInfoMsg;
                        // plan is to add room naming capability once other functionalities are fleshed out.
                        synchronized (roomListDataLock) {
                            roomListArrayMap.put(sid, roomListValues);
                            roomListCsvMap.put(sid,roomListCsv);
                        }

                        out.write(connectionInfoMsg);
                        // stream work done.
                        out.flush();
                        out.close();
                        in.close();
                        // work done, time to exit.
                        break;
                    }

                    case (Requests.LIST_ROOMS_REQ): {
                        /* we enter here if the incoming messages
                         * pertains to a rooms list request.
                         */
                        RoomsListHandler rlh = new RoomsListHandler(socket);
                        rlh.start();
                        break;
                    }

                    case (Requests.JOIN_ROOM_REQ): {
                        /**
                         * we enter here when a user is requesting to join an existing room. 
                         */
                        // TODO implement this!
                        break;
                    }
                    
                    default: {
                        System.out.println("RequestHandler -> default case.");
                    }
                }

            } catch (IOException e) {
                System.out.println("RequestHandler IO Error! -->" + e.getMessage());
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