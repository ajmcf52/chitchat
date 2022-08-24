package requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import main.ApplicationState;
import main.AppStateValue;
import misc.Worker;
import net.ChatUser;
import misc.Requests;
import misc.Constants;

/**
 * this class performs the leg work of getting  
 * a user into another user's chat room.
 */
public class JoinRoomWorker extends Worker {

    private String sessionIP; // ip of the SessionCoordinator in charge of the room being joined
    private int sessionPort; // port "" "" 
    private ChatUser userJoining; // alias of the user requesting to join
    private Object chatUserLock; // used to notify ChatUser when crucial actions have been completed
    private ApplicationState appState; // state of the application
    
    /**
     * constructor for JRW
     * @param workerNum unique number identifying this worker within its worker class
     * @param connectInfo connection info for SessionCoordinator of the room being joined
     * @param userJoin ChatUser requesting to join (i.e., Bob)
     * @param chatUserLock used to notify the chat user when crucial actions are finished
     * 
     * NOTE we operate on the precondition that connectInfo being passed in
        is of the form --> "<Valid IP address>:<Valid port>"
          
        This input is not user-supplied, it is programmed, and so I choose to leave
        it up to the correctness of my code to handle this. If this constructor breaks
        for ArrayOutOfBounds or NumberFormat, then the bug(s) should be easy to track down.
     */
    public JoinRoomWorker(int workerNum, String connectInfo, ChatUser userJoin, Object userLock, ApplicationState state) {
        super("JRW-" + Integer.toString(workerNum));
        String[] connectionArgs = connectInfo.split(":");
        sessionIP = connectionArgs[0].startsWith("0.0.0.0") ? "localhost" : connectionArgs[0];
        sessionPort = Integer.parseInt(connectionArgs[1]);
        userJoining = userJoin;
        chatUserLock = userLock;
        appState = state;
    }

    /**
     * this thread's main line of execution
     */
    public void run() {
        Socket socket = null;

        try {

            socket = new Socket(InetAddress.getByName(sessionIP), sessionPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new PrintWriter(socket.getOutputStream()));
            // send the initial request message, "JOIN_ROOM_REQUEST <alias of user requesting to join>\n"
            String requestMessage = Requests.JOIN_ROOM_REQ + Constants.DELIM + userJoining.getAlias() + '\n';
            out.write(requestMessage);
            out.flush();
            // read the response (should just read "OK")
            String response = in.readLine();
            if (response.equals("OK")) {
                System.out.println("Exchange with SessionCoordinator for " + userJoining.getAlias() + " all good.");
            }
            else {
                System.out.println("hmmmm...");
            }
            // work done; initialize ChatUser field info, notify, close streams (keep socket open for ChatUser) and exit.
            userJoining.initializeSessionSocket(socket);
            in.close();
            out.close();
            appState.setAppState(AppStateValue.CHATTING);
            userJoining.setSessionHostValue(Constants.CHATTING);
            
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }
            

        } catch (Exception e) {
            System.out.println(workerID + " Error! --> " + e.getMessage());
        }
    }
}
