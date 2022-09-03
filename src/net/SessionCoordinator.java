package net;

import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;

import io.OutputWorker;
import io.session.SessionInputWorker;
import io.session.MessageRouter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
 */
public class SessionCoordinator extends Worker {

    private static final int BACKLOG = 20;

    private static ArrayList<ArrayBlockingQueue<Message>> incomingMessageQueues; // incoming message queues
    private static ArrayList<ArrayBlockingQueue<Message>> outgoingMessageQueues; // outgoing message queues
    private static ArrayBlockingQueue<Integer> taskQueue; // the singular task queue

    private ArrayList<Socket> chatRoomUserSockets; // sockets of all the users in the given chat room.
    private static ArrayList<Object> newMessageNotifiers; // waited on by OutputWorkers for new messages

    private ArrayList<SessionInputWorker> inputWorkers; // thread-based workers responsible for reading in new messages.
    private ArrayList<OutputWorker> outputWorkers; // thread-based workers responsible for writing outgoing messages.
    private ArrayList<MessageRouter> messageRouters; // thread-based workers responsible for forwarding messages (in to
                                                     // out)

    private ServerSocket connectionReceiver; // socket used to receive new connections to the chat session.
    private int participantCount; // number of users in the chat room.
    private String roomName; // id of the session this coordinator is in charge of.
    private String hostAlias; // host alias String.

    private ArrayList<String> participantList; // names of all the users currently in the chat session
    private HashMap<String, Integer> aliasWorkerNumberMappings; // maps alias Strings to the ID number allocated to
                                                                // workers responsible for said user.

    /**
     * constructor for the SessionCoordinator
     * 
     * @param workerNum  number unique to this worker within its class
     * @param serveSock  server socket that will be used to accept incoming user
     *                       connections to the chat room
     * @param hostAli    alias of the intended chat room host
     * @param nameOfRoom name of the room
     */
    public SessionCoordinator(int workerNum, ServerSocket serveSock, String hostAli, String nameOfRoom) {
        super("SC-" + Integer.toString(workerNum));
        connectionReceiver = serveSock;
        incomingMessageQueues = new ArrayList<ArrayBlockingQueue<Message>>();
        outgoingMessageQueues = new ArrayList<ArrayBlockingQueue<Message>>();
        taskQueue = new ArrayBlockingQueue<Integer>(BACKLOG, true);
        chatRoomUserSockets = new ArrayList<Socket>();
        newMessageNotifiers = new ArrayList<Object>();
        inputWorkers = new ArrayList<SessionInputWorker>();
        outputWorkers = new ArrayList<OutputWorker>();
        messageRouters = new ArrayList<MessageRouter>();
        participantCount = 0;
        roomName = nameOfRoom;
        hostAlias = hostAli;
        participantList = new ArrayList<String>();
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

        return participantCount == registryParticipantCount;
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
            if (msg instanceof JoinRoomMessage) {
                /**
                 * in the case of a JoinRoom request, we simply initialize the user. everything
                 * is taken care of in that function.
                 */
                JoinRoomMessage jrm = (JoinRoomMessage) msg;
                String alias = jrm.getUserJoining();
                initializeUser(alias, socket, false, in, out);

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
            } else if (msg instanceof ExitRoomMessage) {
                ExitRoomMessage erm = (ExitRoomMessage) msg;

                int workerNum = 0; // NOTE if we don't enter, we just remove host's workers.
                ArrayBlockingQueue<Message> q = incomingMessageQueues.get(workerNum);
                String alias = erm.getExitingUser();

                // if there is more than one user, notify others of the exit.
                if (participantCount > 1) {
                    String roomName = erm.getAssociatedRoom();
                    ExitNotifyMessage enm = new ExitNotifyMessage(alias, roomName);
                    workerNum = aliasWorkerNumberMappings.get(alias);
                    q.add(enm);
                }

                // in any case, send out the ERM response back to the ChatUser leaving.
                q.add(erm);
                try {
                    taskQueue.add(workerNum);
                } catch (Exception e) {
                    System.out.println(workerID + " error placing ExitNotify task --> " + e.getMessage());
                }

                try {
                    sleep(1000); // give the workers a second to send out the previously queued messages.
                } catch (Exception e) {
                    System.out.println(workerID + " interrupted while sleeping --> " + e.getMessage());
                }

                // ensure proper shut down of workers associated with user leaving
                shutDownWorkers(workerNum);

                // update remaining data structures
                Socket s = chatRoomUserSockets.remove(workerNum);
                try {
                    s.close();
                } catch (Exception e) {
                    System.out.println(workerID + " error closing socket-" + workerNum + " --> " + e.getMessage());
                }

                incomingMessageQueues.remove(workerNum);
                outgoingMessageQueues.remove(workerNum);
                newMessageNotifiers.remove(workerNum);

                participantList.remove(workerNum);
                aliasWorkerNumberMappings.remove(alias);
                participantCount--;

                // communicate participant changes with Registry
                try {
                    rSocket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                    rOut = new ObjectOutputStream(rSocket.getOutputStream());
                    rIn = new ObjectInputStream(rSocket.getInputStream());

                    // forward the ERM
                    rOut.writeObject(erm);
                    rOut.flush();

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

            // in either case, close the registry streams and socket.
            if (rSocket.isConnected()) {
                try {
                    rSocket.close(); // NOTE closing the socket also closes the streams as well.
                } catch (Exception e) {
                    System.out.println(workerID + " error closing comms with Registry --> " + e.getMessage());
                }
            }

            /*
             * if the room is now empty, this SessionCoordinator can shut down.
             */
            if (participantCount == 0) {
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
     * @param workerNum integer value corresponding to a set of workers.
     */
    public void shutDownWorkers(int workerNum) {
        if (workerNum < 0 || workerNum >= participantCount)
            throw new IndexOutOfBoundsException();

        MessageRouter mr = messageRouters.remove(workerNum);
        mr.turnOff();
        mr.interrupt();

        SessionInputWorker siw = inputWorkers.remove(workerNum);
        siw.turnOff();
        siw.interrupt();

        OutputWorker ow = outputWorkers.remove(workerNum);
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

        /**
         * in any case, we need to initialize some field variables to open up some
         * communications pathways, both for hosts and non-hosts alike.
         */
        ArrayBlockingQueue<Message> incoming = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH, true);
        ArrayBlockingQueue<Message> outgoing = new ArrayBlockingQueue<Message>(Constants.MSG_QUEUE_LENGTH, true);
        newMessageNotifiers.add(new Object());

        SessionInputWorker inputWorker = new SessionInputWorker(participantCount, in, incoming, taskQueue);
        String workerCode = "S" + Integer.toString(participantCount);

        OutputWorker outputWorker = new OutputWorker(workerCode, out, outgoing,
                        newMessageNotifiers.get(participantCount));
        MessageRouter messageRouter = new MessageRouter(participantCount, taskQueue, incomingMessageQueues,
                        outgoingMessageQueues, newMessageNotifiers);

        // perform book-keeping
        incomingMessageQueues.add(incoming);
        outgoingMessageQueues.add(outgoing);
        chatRoomUserSockets.add(socket);
        inputWorkers.add(inputWorker);
        outputWorkers.add(outputWorker);
        messageRouters.add(messageRouter);
        aliasWorkerNumberMappings.put(alias, participantCount);

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
                taskQueued = taskQueue.add(participantCount);
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
            welcoming = new WelcomeMessage(alias, roomName, false, participantList);
            JoinNotifyMessage joinNotify = new JoinNotifyMessage(alias, roomName);
            ArrayBlockingQueue<Message> q = incomingMessageQueues.get(participantCount);
            q.add(joinNotify);
            q.add(welcoming);
            try {
                taskQueue.put(participantCount);
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
        outputWorkers.get(participantCount).start();
        inputWorkers.get(participantCount).start();
        messageRouters.get(participantCount).start();

        participantList.add(alias);
        participantCount++;
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
