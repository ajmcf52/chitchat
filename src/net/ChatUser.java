package net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import misc.Constants;
import ui.ChatWindow;
import worker.user.*;
import worker.OutputWorker;

/**
 * this class represents a thread-based ChatUser within the Chatter application.
 */
public class ChatUser extends Thread {
    private String userID; //uniquely identifies this user.
    private String alias; // this chat user's screen name.
    private String sessionInetAddress; // inet address of a chat session's server socket.
    private int sessionPort; // port of the chat session's server socket.

    /**
     * worker responsible for reading incoming messages from the SessionCoordinator.
     * when a message is received 
     */
    private UserInputWorker inputWorker; // receives incoming messages and passes them to the handler.
    private OutputWorker outputWorker; // sends outgoing messages to SeshCoordinator.
    private UserInputHandler inputHandler; //receives new messages from IW and handles them accordingly.
    private ChatWindow chatWindowRef; 

    /**
     * default constructor.
     */
    public ChatUser() {
        userID = "";
        alias = "";
        sessionInetAddress = "";
        sessionPort = -1;

        inputWorker = null;
        outputWorker = null;
        inputHandler = null;
        chatWindowRef = null;
    }

    /**
     * constructor that will more than likely be used most of the time.
     * @param uid - user ID
     * @param a - alias
     */
    public ChatUser(String uid, String a) {
        userID = uid;
        alias = a;
        sessionInetAddress = "";
        sessionPort = -1;
    }

    /**
     * the ChatUser's main course of action.
     */
    public void run() {
        if (sessionInetAddress == "") {
            System.out.println("ChatUser cannot proceed, no session address to connect to!");
            return;
        }
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            socket = new Socket(sessionInetAddress, sessionPort); // connecting to SessionCoordinator here.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

        } catch (Exception e) {
            System.out.println("Error in ChatUser I/O! --> " + e.getMessage());
        }
        ArrayBlockingQueue<String> msgQueue = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH, true);
        inputHandler = new UserInputHandler(chatWindowRef, msgQueue);
        inputWorker = new UserInputWorker(in, msgQueue);
        inputHandler.start();
        inputWorker.start();

        // TODO work on this next time!!!
        //outputWorker = new OutputWorker(out, msgQueue)
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
}
