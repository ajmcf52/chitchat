package net;

import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;

import io.OutputWorker;
import io.session.SessionInputWorker;
import io.session.MessageRouter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import misc.Constants;
import misc.ValidateInput;
import misc.Worker;
import messages.ExitNotifyMessage;
import messages.ExitRoomMessage;
import messages.JoinNotifyMessage;
import messages.JoinRoomMessage;
import messages.Message;
import messages.SimpleMessage;
import messages.WelcomeMessage;

/**
 * The role of this class is to coordinate the sending and receiving of chat
 * messages to-and-from the various ChatUsers in a given chat session.
 * 
 * In short, SessionCoordinator is truly only responsible for handling the entry
 * and exit of ChatUsers into and out of the chat room that they are responsible
 * for. During both of these processes, the SC mostly performs thread and
 * resource management; the actual sending and receiving of messages to and from
 * users within the chat is handled <i>completely</i> by workers dispatched by
 * the SC; namely, SessionInputWorker, OutputWorker, and MessageRouter.
 * 
 * SIW and OW read from and write to sockets, pushing to and pulling from
 * Message-based thread-safe queues (i.e., ArrayBlockingQueues). MessageRouter
 * is the entity that connects their workflows together.
 * 
 * All SIWs also have access to a singular global task queue. In this context,
 * tasks are seen as routing numbers. Every I/O pipeline that gets opened up for
 * a ChatUser is assigned a routing number; a task with a given routing number X
 * indicates that the SIW's queue that corresponds with said routing number has
 * one or more Messages that require forwarding.
 * 
 * MessageRouters wait around for work to do. When a SIW pushes a newly received
 * Message into their incoming message queue, they also push a task (i.e., their
 * routing number) into the global task queue. A MessageRouter picks this up,
 * goes to check the incoming message queue that corresponds with said routing
 * number, and forwards those Message(s) to all other outgoing queues in a
 * 1-to-N format, skipping of course the outgoing queue that corresponds with
 * the originally tasked routing number, as that would lead to users sending
 * Messages to themselves, which is undesirable (with single-shot messages, the
 * routed user is the only receiver. This is explained in Message.java).
 * 
 * When these Messages are forwarded and pushed into their correspondent
 * outgoing message queues, the appropriate OutputWorkers wake up, see there is
 * work to do, and write these messages out one by one via their provided
 * Socket, which then gets received by every other user in the chat.
 */
public class SessionCoordinator extends Worker {

    private static final int BACKLOG = 20; // maximum # of messages allowed in a queue at one time

    private static HashMap<Integer, ArrayBlockingQueue<Message>> incomingMsgQueueMap; // incoming message queues
    private static HashMap<Integer, ArrayBlockingQueue<Message>> outgoingMsgQueueMap; // outgoing message queues
    private static ArrayBlockingQueue<Integer> taskQueue; // the singular task queue

    private ArrayList<Socket> chatRoomUserSockets; // sockets of all the users in the given chat room.
    private static HashMap<Integer, Object> newMessageNotifiers; // waited on by OutputWorkers for new messages

    private HashMap<Integer, SessionInputWorker> inputWorkers; // workers responsible for reading in new messages.
    private HashMap<Integer, OutputWorker> outputWorkers; // workers responsible for writing outgoing messages.
    private HashMap<Integer, MessageRouter> messageRouters; // workers responsible for forwarding messages (in -> out)

    private ServerSocket connectionReceiver; // socket used to receive new connections to the chat session.
    private int nextRoutingID; // value of the next routing ID number
    private String roomName; // id of the session this coordinator is in charge of.
    private String hostAlias; // host alias String.

    private ArrayList<String> participantList; // names of all the users currently in the chat session
    private HashSet<Integer> activeRoutingIDs; // routing IDs corresponding to users currently in the chat.
    private HashMap<String, Integer> aliasWorkerNumberMappings; // maps alias Strings to the ID number allocated to
                                                                // workers responsible for said user.

    /**
     * constructor for the SessionCoordinator.
     * 
     * @param workerNum  number unique to this worker.
     * @param serveSock  server socket that will be used to accept incoming user
     *                       connections to the chat room
     * @param hostAli    alias of the intended chat room host
     * @param nameOfRoom name of the room
     */
    public SessionCoordinator(int workerNum, ServerSocket serveSock, String hostAli, String nameOfRoom) {
        super("SC-" + Integer.toString(workerNum));
        connectionReceiver = serveSock;
        incomingMsgQueueMap = new HashMap<Integer, ArrayBlockingQueue<Message>>();
        outgoingMsgQueueMap = new HashMap<Integer, ArrayBlockingQueue<Message>>();
        taskQueue = new ArrayBlockingQueue<Integer>(BACKLOG, true);
        chatRoomUserSockets = new ArrayList<Socket>();
        newMessageNotifiers = new HashMap<Integer, Object>();
        inputWorkers = new HashMap<Integer, SessionInputWorker>();
        outputWorkers = new HashMap<Integer, OutputWorker>();
        messageRouters = new HashMap<Integer, MessageRouter>();
        nextRoutingID = 0;
        roomName = nameOfRoom;
        hostAlias = hostAli;
        participantList = new ArrayList<String>();
        activeRoutingIDs = new HashSet<>();
        aliasWorkerNumberMappings = new HashMap<String, Integer>();
    }

    /**
     * used to compare the participant count received from the Registry with what we
     * have. The SM content format is as follows: "OK; <i>participantCount</i> users
     * now chatting."
     * 
     * @param msg SimpleMessage containing the participant count.
     * @return true if the counts match, false otherwise
     * @throw IndexOutOfBoundsException, NumberFormatException
     */
    public boolean checkParticipantCount(SimpleMessage msg) throws IndexOutOfBoundsException, NumberFormatException {
        String[] args = msg.getContent().split(";");
        String chunkOfInterest = args[1].substring(1); // trim first space.
        int registryParticipantCount = Integer.parseInt(chunkOfInterest.split(" ")[0]);

        return activeRoutingIDs.size() == registryParticipantCount;
    }

    public void run() {
        /**
         * the first thing that SessionCoordinator needs to do is set up the host of the
         * room.
         */
        initializeHost(hostAlias);

        /**
         * SessionCoordinator's main responsibility is to wait for & handle Join and
         * Exit messages.
         */
        while (true) {
            Socket socket = null;
            Object obj = null;
            Message msg = null;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;

            // Registry communication fields. (We keep them informed with Joins and Exits)
            Socket rSocket = null;
            ObjectInputStream rIn = null;
            ObjectOutputStream rOut = null;
            try {
                /**
                 * new connection incoming; let's handle it & see what they want.
                 */
                socket = connectionReceiver.accept();
                // NOTE order of constructor calls is crucial here! Reference ChatUser.java for
                // more details.
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                obj = in.readObject();
                msg = ValidateInput.validateMessage(obj);

            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }

            /**
             * in the case of a JoinRoom request, we simply initialize the user. everything
             * is taken care of in that function.
             */
            if (msg instanceof JoinRoomMessage) {

                JoinRoomMessage jrm = (JoinRoomMessage) msg;
                String alias = jrm.getUserJoining();
                initializeUser(alias, socket, false, in, out);

                /**
                 * communicate the user join with the Registry for participant count tracking.
                 */
                try {
                    rSocket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);

                    // NOTE order of constructor calls is crucial here! Reference ChatUser.java for
                    // more details.
                    rOut = new ObjectOutputStream(rSocket.getOutputStream());
                    rIn = new ObjectInputStream(rSocket.getInputStream());

                    // simply forward the join request.
                    rOut.writeObject(jrm);
                    rOut.flush();

                    // we expect to receive a simple "OK" with the new participant count included.
                    obj = rIn.readObject();
                    msg = ValidateInput.validateMessage(obj);
                    if (!(msg instanceof SimpleMessage)) {
                        rSocket.close();
                        throw new ClassCastException("Unexpected response from Registry.");
                    }
                    SimpleMessage sm = (SimpleMessage) msg;

                    // Confirm that the participant count matches what we have.
                    if (!checkParticipantCount(sm)) {
                        System.out.println("Bad participant count received from Registry! Breaking.");
                        break;
                    }

                } catch (Exception e) {
                    System.out.println(workerID + " error communicating Reg for JRM --> " + e.getMessage());
                }
            }

            /**
             * received when a user wishes to exit the chat room.
             */
            else if (msg instanceof ExitRoomMessage) {
                ExitRoomMessage erm = (ExitRoomMessage) msg;

                String alias = erm.getExitingUser();
                int routingNum = aliasWorkerNumberMappings.get(alias);
                ArrayBlockingQueue<Message> q = incomingMsgQueueMap.get(routingNum);

                // if there is more than one user currently, notify others of the exit.
                if (activeRoutingIDs.size() > 1) {

                    String roomName = erm.getAssociatedRoom();
                    ExitNotifyMessage enm = new ExitNotifyMessage(alias, roomName);
                    q.add(enm);
                }

                /**
                 * NOTE Here, the SimpleMessage response is being written to the ExitRoomWorker,
                 * and the ERM is being forwarded to the exiting ChatUser's InputHandler.
                 * 
                 * Perhaps a tad overcomplicated, but this is how I was able to get the exit
                 * procedure to work within the context of my code base.
                 */
                String responseText = "OK";
                SimpleMessage response = new SimpleMessage(alias, responseText);
                try {
                    out.writeObject(response);
                    out.flush();
                    q.add(erm);
                } catch (Exception e) {
                    System.out.println(workerID + " error replying to ERM --> " + e.getMessage());
                }
                taskQueue.add(routingNum);

                // ensure proper shut down of workers associated with user leaving
                // shutDownWorkers(routingNum);
                MessageRouter mr = messageRouters.remove(routingNum);
                OutputWorker ow = outputWorkers.remove(routingNum);

                /**
                 * NOTE both of these workers have code that allows them to self-detect when to
                 * shut themselves done based on checking messages as they are sent out.
                 * 
                 * For instance, if an outgoing message is an ERM, they know to shut down, no
                 * interrupt required.
                 */
                try {
                    mr.join();
                    ow.join();
                } catch (Exception e) {
                    System.out.println("Error joining on MR and OW in shut down procedure.");
                }
                Socket s = chatRoomUserSockets.remove(routingNum);
                SessionInputWorker siw = inputWorkers.remove(routingNum);
                siw.turnOff();
                try {
                    s.close();
                    siw.join();
                } catch (Exception e) {
                    System.out.println("Error joining on SIW in shut down procedure.");
                }

                incomingMsgQueueMap.remove(routingNum);
                outgoingMsgQueueMap.remove(routingNum);
                newMessageNotifiers.remove(routingNum);

                participantList.remove(routingNum);
                aliasWorkerNumberMappings.remove(alias);
                activeRoutingIDs.remove(routingNum);

                /**
                 * communicate participant changes with Registry.
                 */
                try {
                    rSocket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                    rOut = new ObjectOutputStream(rSocket.getOutputStream());
                    rIn = new ObjectInputStream(rSocket.getInputStream());

                    // forward the ERM
                    rOut.writeObject(erm);
                    rOut.flush();

                    // read the reply
                    obj = rIn.readObject();
                    msg = ValidateInput.validateMessage(obj);
                    if (!(msg instanceof SimpleMessage)) {
                        rSocket.close();
                        throw new ClassCastException("Unexpected response from Registry");
                    }
                    SimpleMessage sm = (SimpleMessage) msg;
                    if (!checkParticipantCount(sm)) {
                        System.out.println("Non-matching participant count received from Registry..");
                        break;
                    }

                } catch (Exception e) {
                    System.out.println(workerID + " connecting to Registry for ERM --> " + e.getMessage());
                }
            }

            // in any case, close the registry socket and associated streams.
            if (rSocket.isConnected()) {
                try {
                    rSocket.close();
                } catch (Exception e) {
                    System.out.println(workerID + " error closing comms with Registry --> " + e.getMessage());
                }
            }

            /*
             * if the room is now empty, this SessionCoordinator can shut down.
             */
            if (activeRoutingIDs.size() == 0) {
                System.out.println(workerID + " room empty; shutting down.");
                try {
                    connectionReceiver.close();
                } catch (Exception e) {
                    System.out.println(workerID + " error closing ServerSocket --> " + e.getMessage());
                }
                break;
            }

        } // end of main loop

    } // end of run()

    /**
     * takes the necessary steps to shut down workers associated with the provided
     * digit.
     * 
     * @param routingID integer value corresponding to a set of workers.
     * 
     * @deprecated other measures were taken to shut SC workers down.
     */
    public void shutDownWorkers(int routingID) {
        if (routingID < 0 || routingID >= activeRoutingIDs.size())
            throw new IndexOutOfBoundsException();

        MessageRouter mr = messageRouters.remove(routingID);
        mr.turnOff();
        mr.interrupt();

        SessionInputWorker siw = inputWorkers.remove(routingID);
        siw.turnOff();
        siw.interrupt();

        OutputWorker ow = outputWorkers.remove(routingID);
        ow.turnOff();
        ow.interrupt();

        try {
            mr.join();
            siw.join();
            ow.join();
        } catch (Exception e) {
            System.out.println("Difficulty shutting down SC workers --> " + e.getMessage());
        }

    }

    /**
     * this method is called to initialized a particular user to the chat room.
     * 
     * @param alias          name of the user
     * @param socket         socket that is connected to the user
     * @param initialMessage first message to be sent to all users in the room.
     *                           Hosts receive a special version.
     * @param in             input stream
     * @param out            output stream
     * 
     *                           NOTE while it isn't completely necessary, we pass
     *                           the input/output streams in as parameters, as they
     *                           were already created previously and it makes no
     *                           sense to re-create the stream objects.
     */
    public void initializeUser(String alias, Socket socket, boolean isHosting, ObjectInputStream in,
                    ObjectOutputStream out) {

        if (!isHosting) {
            /**
             * if isHosting is false, this message is sent to a JoinRoomWorker; all they
             * expect in return is a SimpleMessage with the text "OK". (ChatUser's
             * WelcomeMessage is handled and sent later in the function)
             */
            String responseMsgContent = "OK";
            SimpleMessage responseMsg = new SimpleMessage(alias, responseMsgContent);

            try {
                out.writeObject(responseMsg);
                out.flush();
            } catch (Exception e) {
                System.out.println(workerID + " error responding to JoinRequest --> " + e.getMessage());
            }
        }

        int routingIdNumber = nextRoutingID++;

        /**
         * in any case, we need to initialize some field variables to open up some
         * communications pathways, both for hosts and non-hosts alike.
         */
        ArrayBlockingQueue<Message> incoming = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH, true);
        ArrayBlockingQueue<Message> outgoing = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH, true);
        newMessageNotifiers.put(routingIdNumber, new Object());

        SessionInputWorker inputWorker = new SessionInputWorker(routingIdNumber, in, incoming, taskQueue);
        String workerCode = "S" + Integer.toString(routingIdNumber);

        OutputWorker outputWorker = new OutputWorker(workerCode, out, outgoing,
                        newMessageNotifiers.get(routingIdNumber));
        MessageRouter messageRouter = new MessageRouter(routingIdNumber, taskQueue, incomingMsgQueueMap,
                        outgoingMsgQueueMap, newMessageNotifiers);

        // perform book-keeping
        incomingMsgQueueMap.put(routingIdNumber, incoming);
        outgoingMsgQueueMap.put(routingIdNumber, outgoing);
        chatRoomUserSockets.add(socket);
        inputWorkers.put(routingIdNumber, inputWorker);
        outputWorkers.put(routingIdNumber, outputWorker);
        messageRouters.put(routingIdNumber, messageRouter);
        activeRoutingIDs.add(routingIdNumber);
        aliasWorkerNumberMappings.put(alias, routingIdNumber);

        WelcomeMessage welcoming = null;
        if (isHosting) {
            // in this case, we queue up the sending of a single host-centric
            // WelcomeMessage.
            welcoming = new WelcomeMessage(alias, roomName, isHosting);
            /**
             * NOTE we perform a bit of a hack here. We bypass the reception of a message
             * and simply pretend we receive the message, when really, as Coordinator, we
             * are interjecting and passing a special request into the incoming queue for a
             * MessageRouter to handle.
             */
            incoming.add(welcoming);
            boolean taskQueued = false;
            while (true) {
                taskQueued = taskQueue.add(routingIdNumber);
                if (taskQueued)
                    break;
                else {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println("Task queue sleep interrupted --> " + e.getMessage());
                    }
                }
            }
        } else {
            /**
             * Enter here if isHosting is false. In this case, we send a WelcomeMessage to
             * the user that just joined (not host-centric), as well as a JoinNotifyMessage
             * to all others in the chat.
             */
            welcoming = new WelcomeMessage(alias, roomName, isHosting, participantList);
            JoinNotifyMessage joinNotify = new JoinNotifyMessage(alias, roomName);
            ArrayBlockingQueue<Message> q = incomingMsgQueueMap.get(routingIdNumber);
            q.add(joinNotify);
            q.add(welcoming);
            try {
                taskQueue.put(routingIdNumber);
            } catch (Exception e) {
                System.out.println(workerID + " error placing tasking into Q --> " + e.getMessage());
            }
            /**
             * NOTE by definition, WelcomeMessages are "single-shot" (i.e., they're only
             * sent to a single target user), whereas JoinNotifyMessages aren't; they're
             * sent to everyone BUT the person being welcomed. So by defining this key
             * characteristic that defines a major difference between these Messages, we can
             * accomplish the sending of N messages by only placing 2 messages into the
             * outgoing queue.
             */
        }

        // fire up worker threads for the user that just joined.
        outputWorkers.get(routingIdNumber).start();
        inputWorkers.get(routingIdNumber).start();
        messageRouters.get(routingIdNumber).start();

        participantList.add(alias);
    }

    /**
     * method used to initialize the host communication pathways for the chat room.
     * 
     * @param hostAlias alias of the host user
     */
    public void initializeHost(String hostAlias) {
        // ChatUser will be attempting to connect to the ServerSocket at this point.

        // declare variables.
        Socket socket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {

            socket = connectionReceiver.accept();
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

        } catch (Exception e) {
            System.out.println("SessionCoordinator Error! --> " + e.getMessage());
            e.printStackTrace();
        }

        // host is a user too.
        initializeUser(hostAlias, socket, true, in, out);
    }

    /**
     * getter for participant list.
     * 
     * @return participant list
     */
    public ArrayList<String> getParticipants() {
        return participantList;
    }
}
