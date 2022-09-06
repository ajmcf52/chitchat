package net;

import io.OutputWorker;
import io.user.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import main.AppStateValue;
import main.ApplicationState;
import messages.ExitRoomMessage;
import messages.JoinRoomMessage;
import messages.Message;
import messages.SimpleMessage;
import misc.Constants;
import misc.ValidateInput;
import ui.ChatWindow;

/**
 * this class represents a thread-based ChatUser within the Chatter application.
 */
public class ChatUser extends Thread {

    private String userID; // number assigned to this user.
    private String alias; // user-chosen screen name; userID is attached at the end to ensure uniqueness.
    private String sessionIP; // ip address of a chat session's server socket.
    private int sessionPort; // port of the chat session's server socket.
    private Socket sessionSocket; // socket for the session.
    private boolean isHost; // true if hosting, false if not
    private boolean isChatting; // true if currently in a chat (or joining one), false otherwise
    private String roomName; // name of the room that this user is currently in or trying to join (can be "")

    private volatile boolean isRunning; // used to indicate when this thread should shut down.
    private final Object runLock = new Object(); // lock object for accessing run flag
    private Object chatUserLock; // notified by outside forces to communicate with this user.
    private Object mainAppNotifier; // used to notify main() of changes in state.

    /*
     * Used as a comms mechanism between UserOutputHandler and OutputWorker.
     */
    private static final Object outgoingMsgNotifier = new Object();

    /*
     * Used as a comms mechanism between UserInputWorker & UserInputHandler.
     */
    private static final Object incomingMsgNotifier = new Object();

    private UserInputWorker inputWorker; // receives incoming messages and passes them to the handler.
    private OutputWorker outputWorker; // sends outgoing messages to SeshCoordinator.
    private UserInputHandler inputHandler; // receives new messages from UIW and handles them accordingly.
    private ChatWindow chatWindowRef; // a reference object to the front-facing chat window.

    private ObjectInputStream in; // input stream
    private ObjectOutputStream out; // output stream

    private ApplicationState appState;

    /**
     * default constructor for ChatUser.
     * 
     * @param cul   chat user lock
     * @param man   AKA, main App Notifier
     * @param state state of the application. Used to influence main() control flow.
     */
    public ChatUser(Object cul, Object man, ApplicationState state) {
        userID = "";
        alias = "";
        sessionIP = "";
        roomName = "";
        sessionPort = -1;
        sessionSocket = null;
        isHost = false;
        isRunning = false;
        chatUserLock = cul;
        mainAppNotifier = man;

        inputWorker = null;
        outputWorker = null;
        inputHandler = null;
        chatWindowRef = null;

        appState = state;
    }

    /**
     * This method is called to join a chat room. The fields involved performing the
     * actual joining of the room (namely, the room name, host ip, & port) are all
     * initialized by JoinRoomWorker, a Thread worker that gets fired up in
     * RoomSelectPanel.java
     */
    public void joinRoom() {
        try {
            sessionSocket = new Socket(sessionIP, sessionPort);

            /**
             * NOTE the order in which these constructors are called is important. See here
             * for more info:
             * https://stackoverflow.com/questions/14110986/new-objectinputstream-blocks
             */
            out = new ObjectOutputStream(sessionSocket.getOutputStream());
            in = new ObjectInputStream(sessionSocket.getInputStream());

            if (!isHost) {
                /**
                 * if we aren't the host of the room we're joining, ChatUser needs to perform a
                 * quick exchange with the SessionCoordinator to secure the room join.
                 */

                JoinRoomMessage msg = new JoinRoomMessage(alias, roomName);
                out.writeObject(msg);
                out.flush();
                // read the response (should just read "OK")
                Object obj = in.readObject();
                SimpleMessage response = ValidateInput.validateSimpleMessage(obj);

                /**
                 * NOTE format of SimpleMessage content is: [timestamp] <associated sender
                 * alias>: <text> we only want the text. The code directly below does exactly
                 * that.
                 */
                String responseContentOfInterest = response.getContent().split(": ")[1];
                if (!responseContentOfInterest.equals("OK")) {
                    System.out.println("Unexpected Response to JRM --> " + response.getContent());
                }
            }

        } catch (Exception e) {
            System.out.println(alias + " Error --> " + e.getMessage());
        }

    }

    /**
     * NOTE sessionIP, sessionPort, and roomName are all initialized by outside
     * forces: - In the case of joining a chat room, these fields are initialized by
     * a JoinRoomWorker. - In the case of starting a new room, these fields are
     * initialized by a RoomSetupWorker.
     */

    /**
     * the ChatUser's main course of action.
     */
    public void run() {
        isRunning = true;

        while (isRunning) {
            out = null;
            in = null;

            if (isChatting) {
                joinRoom();

                ArrayBlockingQueue<Message> msgQueue = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH,
                                true);
                outputWorker = new OutputWorker(userID, out, msgQueue, outgoingMsgNotifier);
                outputWorker.start();
                inputHandler = new UserInputHandler(chatWindowRef, msgQueue, incomingMsgNotifier, mainAppNotifier,
                                appState);

                // i.e., pulls 1 from "Alice#U01"
                int workerIdNum = Integer.parseInt(alias.split("#U")[1]);
                inputWorker = new UserInputWorker(workerIdNum, in, msgQueue, incomingMsgNotifier, sessionSocket);
                inputWorker.start();
                inputHandler.start();
            }
            /**
             * In states of chatting & non-chatting, ChatUser just sits around waiting while
             * dispatched threads do all the work.
             */
            try {
                synchronized (chatUserLock) {
                    chatUserLock.wait();
                }
            } catch (Exception e) {
                System.out.println(alias + " encountered an error while waiting --> " + e.getMessage());
            }

            /**
             * if this is true but we have been notified, it means we have left our current
             * chat. Upon joining another chat, we will restart our workers.
             */
            if (isChatting) {
                inputWorker.turnOff();
                inputWorker.interrupt();
                inputHandler.turnOff();
                inputHandler.interrupt();
                outputWorker.turnOff();
                outputWorker.interrupt();

                try {
                    inputWorker.join();
                    inputHandler.join();
                    outputWorker.join();
                } catch (Exception e) {
                    System.out.println(alias + " join() error with worker threads on chat exit --> " + e.getMessage());
                }

                isChatting = false;
            }
        }
    }

    /**
     * used to initialize the identification (UID and alias) of the user.
     * 
     * @param uidMessage A SimpleMessage containing the user's UID within it's
     *                       content.
     * @param a          user alias, not necessarily unique
     *
     *                       NOTE format of the SimpleMessage's content is: "OK; UID
     *                       is <uid>"
     */
    public void initializeID(SimpleMessage uidMessage, String a)
                    throws IndexOutOfBoundsException, NumberFormatException {
        // perform argument manipulation based on the expected standardized
        // SimpleMessage format.
        String[] msgArgs = uidMessage.getContent().split(";");
        userID = msgArgs[1].substring(1).split(" ")[2]; // substring(1) removes the first " ".

        alias = a + "#" + userID; // NOTE this formatting of user alias ensures that every alias is unique.
    }

    /**
     * JoinRoomWorker calls this method to setup information that will be used to
     * connect with the SessionCoordinator for the sake of entering and
     * participating in a chat session.
     * 
     * @param seshInetAddr inet address we are connecting to
     * @param seshPort     port of the SessionCoordinator we are connecting to
     * @param nameOfRoom   name of the room being joined
     */
    public void initSessionInfo(String seshInetAddr, int seshPort, String nameOfRoom) {
        // if the given address is 0.0.0.0, just use localhost instead.
        sessionIP = seshInetAddr.startsWith("0.0.0.0") ? "localhost" : seshInetAddr;
        sessionPort = seshPort;
        roomName = nameOfRoom;
    }

    /**
     * Initializer method for setting up a chat room reference object. This is in
     * place so the ChatUser thread
     * 
     * @param chatWindow
     */
    public void initializeChatRoomRef(ChatWindow chatWindow) {
        chatWindowRef = chatWindow;
    }

    /**
     * initializer for the session Socket of which this user is joining.
     * 
     * @param seshSock Socket connecting this user to the session they are joining.
     * @deprecated initSessionInfo() should be used instead.
     */
    public void initSessionSocket(Socket seshSock) {
        sessionSocket = seshSock;
        initSessionInfo(sessionSocket.getInetAddress().toString(), seshSock.getLocalPort(), "");
    }

    /**
     * getter for userID.
     * 
     * @return userID
     */
    public String getUID() {
        return userID;
    }

    /**
     * getter for roomName.
     * 
     * @return name of the room that this user is in or in the process of joining.
     */
    public String getCurrentRoomName() {
        return roomName;
    }

    /**
     * setter for isHost.
     * 
     * @param hosting hosting boolean value
     */
    public void setHost(boolean hosting) {
        isHost = hosting;
    }

    /**
     * getter for isHost
     * 
     * @return true if hosting, false otherwise
     */
    public boolean isHosting() {
        return isHost;
    }

    /**
     * setter for alias.
     * 
     * @param a - ChatUser's new alias
     */
    public void setAlias(String a) {
        if (a != null && a != "") {
            alias = a;
        }
    }

    /**
     * getter for alias.
     * 
     * @return ChatUser alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * method used by an outside source to signal a shutdown to this user.
     */
    public void signalExit() {
        synchronized (runLock) {
            isRunning = false;
        }
    }

    /**
     * triggers the sending of a message to be seen by other users in the chat.
     * 
     * @param msg message to be sent, timestamp and all.
     */
    public void pushOutgoingMessage(Message msg) {
        outputWorker.triggerMessageSend(msg);
    }

    /**
     * Called by ExitRoomWorker to mark this ChatUser as having officially left the
     * chat. Important call for control flow above.
     */
    public void markChatRoomLeft() {
        isChatting = false;
    }
}
