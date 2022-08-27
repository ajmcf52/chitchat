package io.session;

import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;
import messages.Message;
import misc.ValidateInput;

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
    public SessionInputWorker(int workerNumber, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue, ArrayBlockingQueue<Integer> tq) {
        super("SIW-" + Integer.toString(workerNumber), input, msgQueue);
        taskQueue = tq;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        turnOn();
        
        while (true) {
            try {
                Object obj = in.readObject();
                Message msg = ValidateInput.validateMessage(obj);
                messageQueue.put(msg);
                
                /**
                 * NOTE all worker ID strings follow a strict format. This should be fine.
                 * If it breaks, it should be relatively straightforward as to why it broke
                 * and how to fix it.
                 */
                int workerNum = Integer.parseInt(workerID.split("-")[1]);
                taskQueue.put(workerNum); // queue up the task for BWs

            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }

            // check to see if it is time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }
}
