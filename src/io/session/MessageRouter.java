package io.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import messages.ExitRoomMessage;
import messages.Message;
import messages.SimpleMessage;
import misc.Worker;

/**
 * This class is responsible for the task of forwarding messages from one
 * ChatUser to all other ChatUsers in a given chat session. Each ChatUser in
 * every session is allocated a MessageRouter.
 * 
 * --------------
 * 
 * This is undoubtedly a resource-hungry design and could be made to be much
 * more scalable with even just some form of a basic load balancing scheme. The
 * whole point of "Chatter: Part One" was simply to get the app to as functional
 * of a state as possible. As of today, 9/15/22, I have already been working on
 * Part 1 of this project for 10 weeks (on top of a full-time day job), and so I
 * have decided to cut my losses and proceed on to other things for now.
 * 
 * However, load balancing is certainly something I will likely investigate when
 * I circle back to this application in a few months to add in cloud support.
 * Life is a dynamic beast, though, so nothing is guaranteed. I will say,
 * however, that deploying this app on the cloud will take priority over
 * implementing load balancing, though LB will be integrated as a close second.
 * 
 * Of course, to test the load balancing to some degree, I would need to
 * investigate the development of a ChatterApp testing framework, perhaps even
 * consisting of an N user headless setup configuration. If anything, though,
 * that is a down-the-line discussion.
 * 
 * --------------
 *
 * When an InputWorker has finished placing a newly received message in its
 * outgoing queue, it then places its worker ID number in the chat session task
 * queue.
 *
 * MessageRouters take tasks (i.e., routing ID numbers) from the task queue,
 * which indicate by way of routing ID which queue has messages waiting to be
 * forwarded.
 *
 * By employing ABQs, practically all of our race condition problems are handled
 * by the Java API, at least for message passing.
 */
public class MessageRouter extends Worker {

    private ArrayBlockingQueue<Integer> taskQueue; // where ID numbers representing tasks are placed
    private HashMap<Integer, ArrayBlockingQueue<Message>> incomingMsgQueueMap; // list of incoming message queues
    private HashMap<Integer, ArrayBlockingQueue<Message>> outgoingMsgQueueMap; // list of outgoing message queues
    private HashMap<Integer, Object> newMessageNotifierMap; // list of objects to notify on for messages to be sent.
    private HashSet<Integer> activeWorkerIDs; // a set of the worker IDs corresponding to active ChatUsers.

    /**
     * constructs the MessageRouter.
     * 
     * @param workerNumber number unique to the worker of its class
     * @param tasks        task queue
     * @param in           incoming message queue
     * @param out          outgoing message queue
     * @param notifiers    objects to notify when there are messages to be sent
     * @param workerIds    active worker IDs (i.e., routing numbers)
     */
    public MessageRouter(int workerNumber, ArrayBlockingQueue<Integer> tasks,
                    HashMap<Integer, ArrayBlockingQueue<Message>> in, HashMap<Integer, ArrayBlockingQueue<Message>> out,
                    HashMap<Integer, Object> notifiers, HashSet<Integer> workerIds) {
        super("MR-" + Integer.toString(workerNumber));
        taskQueue = tasks;
        incomingMsgQueueMap = in;
        outgoingMsgQueueMap = out;
        newMessageNotifierMap = notifiers;
        activeWorkerIDs = workerIds;
        // activeWorkerIDs.add(0); // operating on the assumption that the host is
        // active.
    }

    /**
     * this thread's main line of execution.
     */
    public void run() {
        turnOn();

        while (true) {
            Integer task = null;
            try {
                task = taskQueue.take(); // take a task from the task queue
                // execute the task (i.e., forward the messages)

                ArrayList<Message> messagesToFwd = new ArrayList<Message>();
                ArrayBlockingQueue<Message> msgQueue = incomingMsgQueueMap.get(task);
                // retrieve the messages to be forwarded

                /**
                 * if we circle back and there are more messages to forward, get it done. if the
                 * message queue is empty, however, we wait for another task from the task
                 * queue.
                 */
                while (!msgQueue.isEmpty()) {

                    msgQueue.drainTo(messagesToFwd);

                    // forward the messages and notify the appropriate OutputWorker(s)
                    for (Message msg : messagesToFwd) {
                        if (msg.isSingleShot()) {
                            /**
                             * in this case, we only send the Message to the one queue, associated by
                             * routing number. NOTE we ensure that the final ExitRoomMessage is sent out
                             * before the MessageRouter is shut down by getting it to shut itself down upon
                             * detecting an ERM.
                             */
                            if (msg instanceof ExitRoomMessage) {
                                turnOff();
                            }
                            outgoingMsgQueueMap.get(task).add(msg);
                            synchronized (newMessageNotifierMap.get(task)) {
                                newMessageNotifierMap.get(task).notify();
                            }
                        } else {
                            if (msg instanceof SimpleMessage) {
                                System.out.println("here");
                            }
                            /**
                             * in this case we put the Message into everyone's queue EXCEPT for the one
                             * queue associated by task number.
                             */
                            for (Integer i : activeWorkerIDs) {
                                if (task == i)
                                    continue;
                                ArrayBlockingQueue<Message> outgoing = outgoingMsgQueueMap.get(i);
                                outgoing.add(msg);

                                synchronized (newMessageNotifierMap.get(i)) {
                                    newMessageNotifierMap.get(i).notify();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }

            synchronized (runLock) {
                if (!isRunning) {
                    // vocalize shut down in console.
                    proclaimShutdown();
                    break;
                }
            }
        }
    }
}
