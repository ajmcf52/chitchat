package io.session;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;

import java.util.LinkedList;

/**
 * a special type of InputWorker that solely works with the SessionCoordinator.
 */
public class SessionInputWorker extends InputWorker {

    private ArrayBlockingQueue<Integer> taskQueue; // used to queue up tasks for BroadcastWorkers
    
    /**
     * SIW constructor.
     * @param workerNumber unique number assigned to this worker within its class.
     * @param input stream to read messages from
     * @param msgQueue queue where newly received messages are to be placed
     * @param tq task queue, shared amongst all SIWs and BWs
     */
    public SessionInputWorker(int workerNumber, BufferedReader input, ArrayBlockingQueue<String> msgQueue, ArrayBlockingQueue<Integer> tq) {
        super("SIW-" + Integer.toString(workerNumber), input, msgQueue);
        taskQueue = tq;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        // to temporarily hold messages.
        // will only ever be accessed by this worker, therefore no synchronization needed.
        LinkedList<String> messages = new LinkedList<String>(); 
        turnOn();
        
        while (true) {
            

            try {
                /**
                 * Block initially. We intentionally do this so while this thread has no input to read,
                 * it doesn't "busy wait", allowing other threads to make use of available CPU cycles instead.
                 */
                String msg = in.readLine();
                messages.add(msg);
                // while there are messages to read, read them in one by one. ready() does not block.
                while (in.ready()) {
                    messages.add(in.readLine());
                }
                // add all received messages to the ABQ in the order by which they were received.
                while (!messages.isEmpty()) {
                    msg = messages.getFirst();
                    messageQueue.put(msg);
                }
                
                /**
                 * we operate on the precondition that ALL worker ID strings follow the same format,
                 * so this is perfectly legitimate code.
                 */
                int workerNum = Integer.parseInt(workerID.split("-")[1]);
                taskQueue.put(workerNum); // queue up the task for BWs

            } catch (Exception e) {
                System.out.println("SessionInputWorker Error! --> " + e.getMessage());
            }

            // check to see if it is time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    System.out.println("hiya");
                    break;
                }
            }
        }
    }
}
