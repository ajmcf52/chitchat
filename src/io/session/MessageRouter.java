package io.session;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
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
    private ArrayList<ArrayBlockingQueue<Message>> incomingMessageQueues; // list of incoming message queues
    private ArrayList<ArrayBlockingQueue<Message>> outgoingMessageQueues; // list of outgoing message queues
    private ArrayList<Object> newMessageNotifiers; // list of objects to notify on when there are messages to be sent.

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
    public MessageRouter(int workerNumber, ArrayBlockingQueue<Integer> tasks, ArrayList<ArrayBlockingQueue<Message>> in,
                    ArrayList<ArrayBlockingQueue<Message>> out, ArrayList<Object> notifiers) {
        super("MR-" + Integer.toString(workerNumber));
        taskQueue = tasks;
        incomingMessageQueues = in;
        outgoingMessageQueues = out;
        newMessageNotifiers = notifiers;
    }

    /**
     * this thread's main line of execution.
     */
    public void run() {
        while (true) {
            Integer task = null;
            try {
                task = taskQueue.take(); // take a task from the task queue
                // execute the task (i.e., forward the messages)

                ArrayList<Message> messagesToFwd = new ArrayList<Message>();
                ArrayBlockingQueue<Message> msgQueue = incomingMessageQueues.get(task);
                // retrieve the messages to be forwarded
                msgQueue.drainTo(messagesToFwd);

                // forward the messages and notify the appropriate OutputWorker.
                int numUsers = outgoingMessageQueues.size();
                for (Message msg : messagesToFwd) {
                    if (msg.isSingleShot()) {
                        /**
                         * in this case, we only send the Message to the one queue, associated by task
                         * number.
                         */
                        outgoingMessageQueues.get(task).add(msg);
                        synchronized (newMessageNotifiers.get(task)) {
                            newMessageNotifiers.get(task).notify();
                        }
                    } else {
                        /**
                         * in this case we put the Message into everyone's queue EXCEPT for the one
                         * queue, associated by task number.
                         */
                        for (int i = 0; i < numUsers; i++) {
                            if (task == i)
                                continue;
                            ArrayBlockingQueue<Message> outgoing = outgoingMessageQueues.get(i);
                            outgoing.add(msg);

                            synchronized (newMessageNotifiers.get(i)) {
                                newMessageNotifiers.get(i).notify();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }
        }
    }
}
