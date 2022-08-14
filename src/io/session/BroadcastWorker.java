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


    /**
     * constructor for BW.
     * @param tasks task queue
     * @param in incoming message queue
     * @param out outgoing message queue
     */
    public BroadcastWorker(int workerNumber, ArrayBlockingQueue<Integer> tasks, ArrayList<ArrayBlockingQueue<String>> in, ArrayList<ArrayBlockingQueue<String>> out) {
        super("BW-" + Integer.toString(workerNumber));
        taskQueue = tasks;
        incomingMessageQueues = in;
        outgoingMessageQueues = out;
    }

    /**
     * this thread's main line of execution.
     */
    public void run() {
        
    }
}
