package net;

import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;

import io.OutputWorker;
import io.session.SessionInputWorker;
import io.session.MessageRouter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import misc.Constants;
import misc.Requests;
import misc.TimeStampGenerator;
import misc.Worker;

/**
 * The role of this class is to coordinate the sending and receiving of chat messages
 * to-and-from the various ChatUsers in a given chat session.
 */
public class SessionCoordinator extends Worker {

    private static final int BACKLOG = 20;
    
    private static ArrayList<ArrayBlockingQueue<String>> incomingMessageQueues; // incoming message queues
    private static ArrayList<ArrayBlockingQueue<String>> outgoingMessageQueues; // outgoing message queues
    private static ArrayBlockingQueue<Integer> taskQueue; // the singular task queue

    private ArrayList<Socket> chatRoomUserSockets; // sockets of all the users in the given chat room.
    private static ArrayList<Object> newMessageNotifiers; // waited on by OutputWorkers for new messages

    private ArrayList<SessionInputWorker> inputWorkers; // thread-based workers responsible for reading in new messages.
    private ArrayList<OutputWorker> outputWorkers; // thread-based workers responsible for writing outgoing messages.
    private ArrayList<MessageRouter> messageRouters; // thread-based workers responsible for forwarding messages (in to out)

    private ServerSocket connectionReceiver; // socket used to receive new connections to the chat session.
    private int participantCount; // number of users in the chat room.
    private int serverPort; // port on which this server is listening.
    private String roomName; // id of the session this coordinator is in charge of.
    private String hostAlias; // host alias String.

    private HashMap<String, Integer> aliasWorkerNumberMappings; // maps alias Strings to the ID number allocated to workers responsible for said user.

    /**
     * constructor for the SessionCoordinator
     * @param workerNum number unique to this worker within its class
     * @param serveSock server socket that will be used to accept incoming user connections to the chat room
     * @param hostAli alias of the intended chat room host
     * @param nameOfRoom name of the room
     */
    public SessionCoordinator(int workerNum, ServerSocket serveSock, String hostAli, String nameOfRoom) {
        super("SC-" + Integer.toString(workerNum));
        connectionReceiver = serveSock;
        incomingMessageQueues = new ArrayList<ArrayBlockingQueue<String>>();
        outgoingMessageQueues = new ArrayList<ArrayBlockingQueue<String>>();
        taskQueue = new ArrayBlockingQueue<Integer>(BACKLOG, true);
        chatRoomUserSockets = new ArrayList<Socket>();
        newMessageNotifiers = new ArrayList<Object>();
        inputWorkers = new ArrayList<SessionInputWorker>();
        outputWorkers = new ArrayList<OutputWorker>();
        messageRouters = new ArrayList<MessageRouter>();
        serverPort = connectionReceiver.getLocalPort();
        participantCount = 0;
        roomName = nameOfRoom;
        hostAlias = hostAli;
        aliasWorkerNumberMappings = new HashMap<String,Integer>();
    }


    public void run() {
        initializeHost(hostAlias);

        // once the host is initialized, we simply block and wait for new join or leave messages to come in.
        while (true) {
            Socket socket = null;
            String msg = "";
            try {
                socket = connectionReceiver.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                msg = in.readLine();
            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }
            String[] msgArgs = msg.split(Constants.DELIM);
                switch (msgArgs[0]) {
                    case (Requests.JOIN_ROOM_REQ): {
                        String timestampString = "[" + TimeStampGenerator.now() + "]";
                        String introduction = msgArgs[1] + " has joined the chat room.";
                        String completeMessage = timestampString + Constants.DELIM + introduction + '\n';

                        initializeUser(msgArgs[1], socket, completeMessage);
                    }
                    case (Requests.LEAVE_ROOM_REQ): {
                        // TODO later when the time comes... Not an immediate priority.
                    }
                }
        }
    }

    /**
     * this method is called to initialized a particular user to the chat room.
     * @param alias name of the user
     * @param socket socket that is connected to the user
     * @param initialMessage first message to be sent to all users in the room. (host message is only sent to the host)
     */
    public void initializeUser(String alias, Socket socket, String initialMessage) {

        // initialize a bunch of stuff
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println(workerID + " Error! --> " + e.getMessage());
        }
        ArrayBlockingQueue<String> incoming = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH,true);
        ArrayBlockingQueue<String> outgoing = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH, true);
        newMessageNotifiers.add(new Object());

        SessionInputWorker inputWorker = new SessionInputWorker(participantCount, in, incoming, taskQueue);
        String workerCode = "S" + Integer.toString(participantCount);

        OutputWorker outputWorker = new OutputWorker(workerCode, out, outgoing, newMessageNotifiers.get(participantCount));
        MessageRouter messageRouter = new MessageRouter(participantCount, taskQueue, 
            incomingMessageQueues, outgoingMessageQueues, newMessageNotifiers);

        // perform book-keeping
        incomingMessageQueues.add(incoming);
        outgoingMessageQueues.add(outgoing);
        chatRoomUserSockets.add(socket);
        inputWorkers.add(inputWorker);
        outputWorkers.add(outputWorker);
        messageRouters.add(messageRouter);
        incomingMessageQueues.get(participantCount).add(initialMessage);
        aliasWorkerNumberMappings.put(alias, participantCount);
            
        // fire up the worker threads (Host is always at index zero!!!)
        inputWorkers.get(participantCount).start();
        outputWorkers.get(participantCount).start();
        messageRouters.get(participantCount).start();

        try {
            taskQueue.put(participantCount);
        } catch (InterruptedException e) {
            System.out.println(workerID + " interrupted while queueing task :(");
        }

        participantCount++;
    }

    /**
     * method used to initialize the host communication pathways for the chat room.
     * @param hostAlias alias of the host user
     */
    public void initializeHost(String hostAlias) {
        // ChatUser will be attempting to connect to the ServerSocket at this point...
        
        Socket socket = null;
        try {
            socket = connectionReceiver.accept();
            // System.out.println("connection accepted");

            // build and format the welcome message
            String timestampString = "[" + TimeStampGenerator.now() + "]";
            String welcoming = "Welcome, " + hostAlias + ". You are the host of: " + roomName;
            String completeWelcomeMessage = Constants.WELCOME_TAG + Constants.DELIM + timestampString + 
            Constants.DELIM + welcoming + '\n';

            initializeUser(hostAlias, socket, completeWelcomeMessage);

        } catch (Exception e) {
            System.out.println("SessionCoordinator Error! --> " + e.getMessage());
            e.printStackTrace();
        }
    }

}
