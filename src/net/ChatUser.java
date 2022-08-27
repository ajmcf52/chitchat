package net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import io.OutputWorker;
import io.user.*;
import messages.Message;
import misc.Constants;
import ui.ChatWindow;

/**
 * this class represents a thread-based ChatUser within the Chatter application.
 */
public class ChatUser extends Thread {
    private String userID; //uniquely identifies this user.
    private String alias; // this chat user's screen name.
    private String sessionInetAddress; // inet address of a chat session's server socket.
    private int sessionPort; // port of the chat session's server socket.
    private Socket sessionSocket; // socket for the session.
    private int sessionHostValue; // 1 -> host of session; 0 -> non-host, but still in a session; -1 -> neither.

    private volatile boolean isRunning; // used to indicate when this thread should shut down.
    private final Object runLock = new Object(); // lock object for accessing run flag
    private Object chatUserLock; // notified by outside forces to communicate with this user.
    private final static Object outgoingMsgNotifier = new Object(); // ChatUser notifies this to tell OutputWorker there is a new message to send.
    private final static Object incomingMsgNotifier = new Object(); // UserInputWorker notifies this to tell UserInputHandler that there is a new message to receive.

    /**
     * worker responsible for reading incoming messages from the SessionCoordinator.
     * when a message is received 
     */
    private UserInputWorker inputWorker; // receives incoming messages and passes them to the handler.
    private OutputWorker outputWorker; // sends outgoing messages to SeshCoordinator.
    private UserInputHandler inputHandler; //receives new messages from UIW and handles them accordingly.
    private ChatWindow chatWindowRef; 

    /**
     * default constructor for ChatUser.
     * @param cul chat user lock
     */
    public ChatUser(Object cul) {
        userID = "";
        alias = "";
        sessionInetAddress = "";
        sessionPort = -1;
        sessionSocket = null;
        sessionHostValue = -1;
        isRunning = false;
        chatUserLock = cul;

        inputWorker = null;
        outputWorker = null;
        inputHandler = null;
        chatWindowRef = null;
    }

    /**
     * the ChatUser's main course of action.
     */
    public void run() {
        isRunning = true;

        while (isRunning) {

            switch (sessionHostValue) {
                // entered when this user is not chatting.
                case Constants.NOT_CHATTING: {
                    try {
                        synchronized (chatUserLock) {
                            chatUserLock.wait();
                        }
                    } catch (Exception e) {
                        System.out.println(alias + " encountered an error while waiting --> " + e.getMessage());
                    }
                }
                // entered right away for non-host chatters (host enters after setup)
                case Constants.CHATTING: {
                    if (alias.equals("bob")) {
                        System.out.println("lets go bobby boy");
                    }
                    BufferedReader in = null;
                    PrintWriter out = null;
                    try {
                        in = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));
                        out = new PrintWriter(sessionSocket.getOutputStream());
                    } catch (Exception e) {
                        System.out.println("Error in ChatUser I/O! --> " + e.getMessage());
                    }
        
                    ArrayBlockingQueue<String> msgQueue = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH, true);
                    inputHandler = new UserInputHandler(chatWindowRef, msgQueue, incomingMsgNotifier);
                    inputWorker = new UserInputWorker(0, in, msgQueue, incomingMsgNotifier);
                    inputWorker.start();
                    inputHandler.start();
                    
                    outputWorker = new OutputWorker(userID, out, msgQueue, outgoingMsgNotifier);
                    outputWorker.start();

                    try {
                        synchronized (chatUserLock) {
                            chatUserLock.wait();
                        }
                    } catch (Exception e) {
                        System.out.println(alias + " encountered an error while waiting --> " + e.getMessage());
                    }
                }
                // entered for setting up session communcation channels
                case Constants.SOCKET_SETUP: {
                    if (sessionInetAddress == "") {
                        System.out.println("ChatUser cannot proceed, no session address to connect to!");
                        return;
                    }
                    if (sessionInetAddress.startsWith("0.0.0.0")) {
                        sessionInetAddress = "localhost";
                    }

                    try {
                        sessionSocket = new Socket(sessionInetAddress, sessionPort); // connecting to SessionCoordinator here.
                    } catch (Exception e) {
                        System.out.println("Error in ChatUser I/O! --> " + e.getMessage());
                        // e.printStackTrace();
                    }
                    /**
                     * now we can move to chat setup.
                     */
                    this.setSessionValue(Constants.CHATTING);
                    break;
                }
                default:
                    System.out.println("This shouldn't execute, ever..");
                    break;
            }
        }
    }

    /**
     * used to initialize the identification (UID and alias) of the user.
     * @param uid uniquely identifiable user ID string
     * @param a user alias, not necessarily unique
     */
    public void initializeID(String uid, String a) {
        System.out.println("Initializing ChatUser " + userID + "...");
        userID = uid;
        alias = a;
    }

    /**
     * this method is used to setup the socket information that
     * will be used to connect with a session thread for the sake
     * of entering and participating in a chat session.
     * @param seshInetAddr inet address of the SessionCoordinator we must connect to participate in a given ChatSession.
     * @param seshPort port of the SessionCoordinator we are connecting to.
     */
    public void initializeSessionInfo(String seshInetAddr, int seshPort) {
        sessionInetAddress = seshInetAddr;
        sessionPort = seshPort;
    }

    /**
     * Initializer method for setting up a chat room reference object.
     * This is in place so the ChatUser thread 
     * @param chatWindow
     */
    public void initializeChatRoomRef(ChatWindow chatWindow) {
        chatWindowRef = chatWindow;
    }

    /**
     * initializer for the session Socket of which this user is joining.
     * @param seshSock Socket connecting this user to the session they are joining.
     */
    public void initializeSessionSocket(Socket seshSock) {
        sessionSocket = seshSock;
        initializeSessionInfo(sessionSocket.getInetAddress().toString(), seshSock.getLocalPort());
    }

    /**
     * getter for userID.
     * @return userID
     */
    public String getUID() { return userID; }

    /**
     * setter for alias.
     * @param a - ChatUser's new alias
     */
    public void setAlias(String a) {
        if (a != null && a != "") {
            alias = a;
        }
    }

    /**
     * getter for alias.
     * @return ChatUser alias
     */
    public String getAlias() { return alias; }

    /**
     * setter for this user's session host value.
     * NOTE value is expected to be in the range of [-1,1].
     * @param value
     */
    public void setSessionValue(int value) throws IllegalArgumentException {
        if (value < -1 || value > 1) {
            throw new IllegalArgumentException("SessionHostValue must be between -1 and 1 inclusive.");
        }
        sessionHostValue = value;
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
     * @param msg message to be sent, timestamp and all.
     */
    public void pushOutgoingMessage(Message msg) {
        outputWorker.triggerMessageSend(msg);
    }
}
