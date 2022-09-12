package io.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import messages.ExitRoomMessage;
import messages.Message;
import misc.Worker;

/**
 * this class is responsible for the task of forwarding messages (in the context
 * of one specific chat session) that are sent by one user to all the other
 * users in the session.
 *
 * When an InputWorker has finished placing a newly received message in its
 * outgoing queue, it then places its worker ID number in the chat session task
 * queue.
 *
 * This class works by essentially having a pool of BWs taking ID numbers from
 * the task queue, which then indicate which queue has messages waiting to be
 * forwarded.
 *
 * By employing ABQs, practically all of our race condition problems are handled
 * via internal mechanisms.
 */
public class MessageRouter extends Worker {

    private ArrayBlockingQueue<Integer> taskQueue; // where ID numbers representing tasks are placed
    private HashMap<Integer, ArrayBlockingQueue<Message>> incomingMsgQueueMap; // list of incoming message queues
    private HashMap<Integer, ArrayBlockingQueue<Message>> outgoingMsgQueueMap; // list of outgoing message queues
    private HashMap<Integer, Object> newMessageNotifierMap; // list of objects to notify on for messages to be sent.
    private HashSet<Integer> activeWorkerIDs; // a set of the worker IDs corresponding to active ChatUsers.

    /**
     * constructor for BW.
     * 
     * @param workerNum number unique to the worker of its class
     * @param tasks     task queue
     * @param in        incoming message queue
     * @param out       outgoing message queue
     * @param notifiers list of objects to notify on when there are messages to be
     *                      sent
     */
    public MessageRouter(int workerNumber, ArrayBlockingQueue<Integer> tasks,
                    HashMap<Integer, ArrayBlockingQueue<Message>> in, HashMap<Integer, ArrayBlockingQueue<Message>> out,
                    HashMap<Integer, Object> notifiers) {
        super("MR-" + Integer.toString(workerNumber));
        taskQueue = tasks;
        incomingMsgQueueMap = in;
        outgoingMsgQueueMap = out;
        newMessageNotifierMap = notifiers;
        activeWorkerIDs = new HashSet<>();
        activeWorkerIDs.add(0); // operating on the assumption that the host is active.
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
                 * NOTE if there are no more messages in the designated message queue, we are
                 * safe to exit the loop.
                 */
                while (!msgQueue.isEmpty()) {

                    msgQueue.drainTo(messagesToFwd);

                    // forward the messages and notify the appropriate OutputWorker(s)
                    for (Message msg : messagesToFwd) {
                        if (msg.isSingleShot()) {
                            /**
                             * in this case, we only send the Message to the one queue, associated by task
                             * number. NOTE we ensure that the final ExitRoomMessage is sent out before the
                             * MessageRouter is shut down by getting it to shut itself down upon detecting
                             * an ERM.
                             */
                            if (msg instanceof ExitRoomMessage) {
                                turnOff();
                            }
                            outgoingMsgQueueMap.get(task).add(msg);
                            synchronized (newMessageNotifierMap.get(task)) {
                                newMessageNotifierMap.get(task).notify();
                            }
                        } else {
                            /**
                             * in this case we put the Message into everyone's queue EXCEPT for the one
                             * queue, associated by task number.
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
                    proclaimShutdown();
                    break;
                }
            }
        }
    }
}
