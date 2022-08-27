package requests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import main.ApplicationState;
import main.AppStateValue;
import misc.Worker;
import net.ChatUser;
import misc.Requests;
import misc.Constants;

import messages.JoinRoomMessage;
import messages.SimpleMessage;

/**
 * this class performs the leg work of getting  
 * a user into another user's chat room.
 */
public class JoinRoomWorker extends Worker {

    private String sessionIP; // ip of the SessionCoordinator in charge of the room being joined
    private int sessionPort; // port "" "" 
    private ChatUser userJoining; // alias of the user requesting to join
    private String roomName; // name of the room being joined
    private Object chatUserLock; // used to notify ChatUser when crucial actions have been completed
    private ApplicationState appState; // state of the application
    
    /**
     * constructor for JRW
     * @param connectInfo connection info for SessionCoordinator of the room being joined
     * @param userJoin ChatUser requesting to join (i.e., Bob)
     * @param nameOfRoom name of the chat room that is being requested for joining
     * @param userLock used to notify the chat user when crucial actions are finished
     * @param state application state
     * 
     * NOTE we operate on the precondition that connectInfo being passed in
        is of the form --> "<Valid IP address>:<Valid port>"
          
        This input is not user-supplied, it is programmed, and so I choose to leave
        it up to the correctness of my code to handle this. If this constructor breaks
        for ArrayOutOfBounds or NumberFormat, then the bug(s) should be easy to track down.
     */
    public JoinRoomWorker(String connectInfo, ChatUser userJoin, String nameOfRoom, Object userLock, ApplicationState state) {
        super("JRW-0");
        String[] connectionArgs = connectInfo.split(":");
        sessionIP = connectionArgs[0].startsWith("0.0.0.0") ? "localhost" : connectionArgs[0];
        sessionPort = Integer.parseInt(connectionArgs[1]);
        userJoining = userJoin;
        roomName = nameOfRoom;
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
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            // send the initial request message, "JOIN_ROOM_REQUEST <alias of user requesting to join>\n"
            JoinRoomMessage msg = new JoinRoomMessage(userJoining.getAlias(), roomName);
            out.writeObject(msg);
            out.flush();
            // read the response (should just read "OK")
            Object response = in.readObject();
            if (!(response instanceof SimpleMessage)) {
                System.out.println("JoinRoomWorker: SimpleMessage OK not received from SC..");
            }
            else {
                SimpleMessage respMsg = (SimpleMessage) response;
                System.out.println(respMsg.print());
            }

            // NOTE it is ChatUser's responsibility to open the socket back up again.
            userJoining.initializeSessionInfo(sessionIP, sessionPort);
            in.close();
            out.close();
            socket.close();

            appState.setAppState(AppStateValue.CHATTING);
            userJoining.setSessionValue(Constants.CHATTING);
            
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }
            

        } catch (Exception e) {
            System.out.println(workerID + " Error! --> " + e.getMessage());
        }
    }
}
