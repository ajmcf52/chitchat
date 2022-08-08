package net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import worker.session.SessionInputWorker;
import worker.OutputWorker;
import misc.Constants;
import misc.TimeStampGenerator;

/**
 * The role of this class is to coordinate the sending and receiving of chat messages
 * to-and-from the various ChatUsers in a given chat session.
 */
public class SessionCoordinator extends Thread {

    private static final int BACKLOG = 20;
    
    /**
     * ABQs used to fetch incoming messages that are then distributed into the outgoing message queues.
     * InputWorkers are responsible for inserting new messages into these queues.
     */
    private ArrayList<ArrayBlockingQueue<String>> incomingMessageQueues;
    /**
     * ABQs used to place outgoing messages that are then retrieved and whisked off by OutputWorkers.
     */
    private ArrayList<ArrayBlockingQueue<String>> outgoingMessageQueues;

    private ArrayList<Socket> chatRoomUserSockets; // sockets of all the users in the given chat room.
    private ArrayList<SessionInputWorker> inputWorkers; // thread-based workers responsible for reading in new messages.
    private ArrayList<OutputWorker> outputWorkers; // thread-based workers responsible for writing outgoing messages.

    private ServerSocket connectionReceiver; // socket used to receive new connections to the chat session.
    private int participantCount; // number of users in the chat room.
    private int serverPort;
    private String sessionID; // id of the session this coordinator is in charge of.
    private String hostAlias; // host alias String.

    /**
     * constructor for the SessionCoordinator
     * @param serverSocket server socket that will be used to accept incoming user connections to the chat room
     * @param hostAli alias of the intended chat room host
     * @param sid session ID
     */
    public SessionCoordinator(int scPort, String hostAli, String sid) {
        incomingMessageQueues = new ArrayList<ArrayBlockingQueue<String>>();
        outgoingMessageQueues = new ArrayList<ArrayBlockingQueue<String>>();
        chatRoomUserSockets = new ArrayList<Socket>();
        inputWorkers = new ArrayList<SessionInputWorker>();
        outputWorkers = new ArrayList<OutputWorker>();
        serverPort = scPort;
        connectionReceiver = null;
        participantCount = 0;
        sessionID = sid;
        hostAlias = hostAli;
        start();
    }

    // TODO re-work initializeHost().

    public void run() {
        initializeHost(hostAlias);

        // do more stuff here (should be looping while waiting to be notified on an object)
    }

    /**
     * method used to initialize the host communication pathways for the chat room.
     * @param hostAlias alias of the host user
     */
    public void initializeHost(String hostAlias) {
        // ChatUser will be attempting to connect to the ServerSocket at this point...
        
        Socket socket = null;
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            connectionReceiver = new ServerSocket(serverPort,BACKLOG,address);
            socket = connectionReceiver.accept();
            System.out.println("connection accepted");
            // build and format the welcome message
            String timestampString = "[" + TimeStampGenerator.now() + "]";
            String welcoming = "Welcome, " + hostAlias + ". You are the host of this room.";
            String sidString = "--" + sessionID + "--";
            String completeWelcomeMessage = Constants.WELCOME_TAG + Constants.DELIM + timestampString + Constants.DELIM + welcoming + 
            Constants.DELIM + sidString + '\n';
            
            // initialize streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            // initialize ABQs for reader and writer threads
            ArrayBlockingQueue<String> incoming = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH,true);
            ArrayBlockingQueue<String> outgoing = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH, true);
            // initialize thread-based workers
            SessionInputWorker inputWorker = new SessionInputWorker(in, incoming);
            OutputWorker outputWorker = new OutputWorker(out, outgoing);

            // perform book-keeping
            incomingMessageQueues.add(incoming);
            outgoingMessageQueues.add(outgoing);
            chatRoomUserSockets.add(socket);
            inputWorkers.add(inputWorker);
            outputWorkers.add(outputWorker);

            // fire up the worker threads (Host is always at index zero!!!)
            inputWorkers.get(Constants.HOST_INDEX).start();
            outputWorkers.get(Constants.HOST_INDEX).start();

            // send the welcome message, followed by the session ID string.
            out.write(completeWelcomeMessage);
            out.flush();


        } catch (Exception e) {
            System.out.println("SessionCoordinator Error! --> " + e.getMessage());
        }
    }

}
