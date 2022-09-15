package worker;

import main.ApplicationState;
import main.AppStateValue;
import misc.Worker;
import net.ChatUser;

/**
 * this class performs the leg work of getting a user into another user's chat
 * room.
 */
public class JoinRoomWorker extends Worker {

    private String sessionIP; // ip of the SessionCoordinator in charge of the room being joined
    private int sessionPort; // port number of the session
    private ChatUser userJoining; // alias of the user requesting to join
    private String roomName; // name of the room being joined
    private Object mainAppLock; // used to notify main() state machine to proceed to next state.
    private ApplicationState appState; // state of the application

    /**
     * constructor for JRW
     * 
     * @param connectInfo connection info for SessionCoordinator of the room being
     *                        joined
     * @param userJoin    ChatUser requesting to join (i.e., Bob)
     * @param nameOfRoom  name of the chat room that is being requested for joining
     * @param userLock    used to notify the chat user when crucial actions are
     *                        finished
     * @param state       application state
     * 
     *                        NOTE we operate on the precondition that connectInfo
     *                        being passed in is of the form --> "<Valid IP
     *                        address>:<Valid port>"
     * 
     *                        This input is not user-supplied, it is programmed, and
     *                        so I choose to leave it up to the correctness of my
     *                        code to handle this. If this constructor breaks for
     *                        ArrayOutOfBounds or NumberFormat, then the bug(s)
     *                        should be easy to track down.
     */
    public JoinRoomWorker(String connectInfo, ChatUser userJoin, String nameOfRoom, Object mainLock,
                    ApplicationState state) {
        super("JRW-0");
        String[] connectionArgs = connectInfo.split(":");
        sessionIP = connectionArgs[0].startsWith("0.0.0.0") ? "localhost" : connectionArgs[0];
        sessionPort = Integer.parseInt(connectionArgs[1]);
        userJoining = userJoin;
        roomName = nameOfRoom;
        mainAppLock = mainLock;
        appState = state;
    }

    /**
     * this thread's main line of execution.
     */
    public void run() {

        userJoining.initSessionInfo(sessionIP, sessionPort, roomName);
        appState.setAppState(AppStateValue.CHATTING);

        synchronized (mainAppLock) {
            mainAppLock.notify(); // allows main() to move to the "CHATTING" state in its state machine.
        }

    }
}
