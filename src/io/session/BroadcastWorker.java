package io.session;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

import misc.Worker;

/**
 * this class is responsible for the task of forwarding
 * messages (in the context of one specific chat session)
 * that are sent by one user to all the other users in the session.
 * 
 * When an InputWorker has finished placing a newly received message
 * in its outgoing queue, it then places its worker ID number in the 
 * chat session task queue.
 * 
 * This class works by essentially having a pool of BWs taking ID numbers
 * from the task queue, which then indicate which queue has messages waiting
 * to be forwarded.
 * 
 * By employing ABQs, practically all of our race condition problems are
 * handled via internal mechanisms.
 */
public class BroadcastWorker extends Worker {
    
    private ArrayBlockingQueue<Integer> taskQueue; // where ID numbers representing tasks are placed
    private ArrayList<ArrayBlockingQueue<String>> incomingMessageQueues; // list of incoming message queues
    private ArrayList<ArrayBlockingQueue<String>> outgoingMessageQueues; // list of outgoing message queues
    private ArrayList<Object> newMessageNotifiers; // list of objects to notify on when there are messages to be sent.

    /**
     * constructor for BW.
     * @param workerNum number unique to the worker of its class
     * @param tasks task queue
     * @param in incoming message queue
     * @param out outgoing message queue
     * @param notifiers list of objects to notify on when there are messages to be sent
     */
    public BroadcastWorker(int workerNumber, ArrayBlockingQueue<Integer> tasks, 
    ArrayList<ArrayBlockingQueue<String>> in, ArrayList<ArrayBlockingQueue<String>> out, ArrayList<Object> notifiers) {
        super("BW-" + Integer.toString(workerNumber));
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
                task = taskQueue.take();  // take a task from the task queue
                // execute the task (i.e., forward the messages)

                ArrayList<String> messagesToFwd = new ArrayList<String>();
                ArrayBlockingQueue<String> msgQueue = incomingMessageQueues.get(task);
                // retrieve the messages to be forwarded
                msgQueue.drainTo(messagesToFwd);

                // forward the messages and notify the appropriate OutputWorker.
                int numUsers = outgoingMessageQueues.size();
                for (int i = 0; i < numUsers; i++) {
                    ArrayBlockingQueue<String> outgoing = outgoingMessageQueues.get(i);
                    outgoing.addAll(messagesToFwd);
                    newMessageNotifiers.get(i).notify();
                }
                
            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }
        }
    }
}
