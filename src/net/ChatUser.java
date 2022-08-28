package net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import io.OutputWorker;
import io.user.*;
import messages.Message;
import messages.SimpleMessage;
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
    private int sessionStateValue; // 0 --> not chatting, 1 --> chatting

    private volatile boolean isRunning; // used to indicate when this thread should shut down.
    private final Object runLock = new Object(); // lock object for accessing run flag
    private Object chatUserLock; // notified by outside forces to communicate with this user.
    private final static Object outgoingMsgNotifier = new Object(); // ChatUser notifies this to tell OutputWorker there is a new message to send.
    private final static Object incomingMsgNotifier = new Object(); // UserInputWorker notifies this to tell UserInputHandler that there is a new message to receive.


    private UserInputWorker inputWorker; // receives incoming messages and passes them to the handler.
    private OutputWorker outputWorker; // sends outgoing messages to SeshCoordinator.
    private UserInputHandler inputHandler; //receives new messages from UIW and handles them accordingly.
    private ChatWindow chatWindowRef; // a reference object to the front-facing chat window.

    private ObjectInputStream in; // input stream
    private ObjectOutputStream out; // output stream

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
        sessionStateValue = -1;
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
            switch (sessionStateValue) { // NOTE this ChatUser state machine runs inside another, more complete state machine.

                case Constants.NOT_CHATTING: {
                    try {
                        synchronized (chatUserLock) {
                            chatUserLock.wait(); // Woken up when the time is ready to start doing something else.
                        }
                    } catch (Exception e) {
                        System.out.println(alias + " encountered an error while waiting --> " + e.getMessage());
                    }
                }
                case Constants.CHATTING: {
                    try {
                        if (sessionSocket == null) { // if socket is still null, initialize it.
                            sessionSocket = new Socket(sessionInetAddress, sessionPort);
                        }
                        /**
                         * NOTE the order in which these constructors are called is very important!
                         * output streams must always be constructed before input streams when dealing
                         * with Object[Input|Output]Streams.
                         * 
                         * This link here sums it up perfectly: 
                         * https://stackoverflow.com/questions/14110986/new-objectinputstream-blocks
                         * 
                         * In reference to the Javadocs, When a new ObjectInputStream is instantiated,
                         * the first thing it tries to do is read a header from the ObjectOutputStream
                         * at the other end. So if both sides attempt to create their ObjectInputStreams
                         * before their ObjectOutputStreams, deadlock is inevitable...
                         */
                        out = new ObjectOutputStream(sessionSocket.getOutputStream());
                        in = new ObjectInputStream(sessionSocket.getInputStream());
                        
                    } catch (Exception e) {
                        System.out.println(alias + ": Error building stream objects. --> " + e.getMessage());
                    }
        
                    ArrayBlockingQueue<Message> msgQueue = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH, true);
                    outputWorker = new OutputWorker(userID, out, msgQueue, outgoingMsgNotifier);
                    outputWorker.start();
                    inputHandler = new UserInputHandler(chatWindowRef, msgQueue, incomingMsgNotifier);
                    int workerIdNum = Integer.valueOf(userID.charAt(userID.length() - 1)) - Constants.ASCII_NUM_DIFF;
                    inputWorker = new UserInputWorker(workerIdNum, in, msgQueue, incomingMsgNotifier);
                    inputWorker.start();
                    inputHandler.start();
                

                    try {
                        synchronized (chatUserLock) {
                            chatUserLock.wait(); // waiting to do something other than chat.
                        }
                    } catch (Exception e) {
                        System.out.println(alias + " encountered an error while waiting --> " + e.getMessage());
                    }
                }
                default:
                    System.out.println("This shouldn't execute, ever..");
                    break;
            }
        }
    }

    /**
     * used to initialize the identification (UID and alias) of the user.
     * @param uidMessage A SimpleMessage containing the user's UID within it's content.
     * @param a user alias, not necessarily unique
     * 
     * NOTE format of the SimpleMessage's content is: "OK; UID is <uid>"
     */
    public void initializeID(SimpleMessage uidMessage, String a) throws IndexOutOfBoundsException, NumberFormatException {
        
        // perform argument manipulation based on the expected standardized SimpleMessage format.
        String[] msgArgs = uidMessage.getContent().split(";");
        userID = msgArgs[1].substring(1).split(" ")[2]; // substring(1) removes the first " ".

        alias = a + "#" + userID; // NOTE this formatting of user alias ensures that every alias is unique.
    }

    /**
     * this method is used to setup the socket information that
     * will be used to connect with a session thread for the sake
     * of entering and participating in a chat session.
     * @param seshInetAddr inet address of the SessionCoordinator we must connect to participate in a given ChatSession.
     * @param seshPort port of the SessionCoordinator we are connecting to.
     */
    public void initSessionInfo(String seshInetAddr, int seshPort) {
        
        // if the given address is 0.0.0.0, just use localhost instead.
        sessionInetAddress = seshInetAddr.startsWith("0.0.0.0") ? "localhost" : seshInetAddr;
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
     * @deprecated this method is no longer used; instead, we call initSessionInfo()
     * directly in other parts of the code, and ChatUser's state machine takes care
     * of all Socket instantiation.
     */
    public void initializeSessionSocket(Socket seshSock) {
        sessionSocket = seshSock;
        initSessionInfo(sessionSocket.getInetAddress().toString(), seshSock.getLocalPort());
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
        sessionStateValue = value;
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
