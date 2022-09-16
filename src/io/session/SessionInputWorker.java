package io.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;
import messages.Message;
import misc.ValidateInput;

/**
 * A special type of InputWorker that solely works with the SessionCoordinator.
 */
public class SessionInputWorker extends InputWorker {

    private ArrayBlockingQueue<Integer> taskQueue; // used to queue up tasks for MessageRouters

    /**
     * SIW constructor.
     * 
     * @param workerNumber unique number assigned to this worker within its class.
     * @param input        stream to read messages from
     * @param msgQueue     queue where newly received messages are to be placed
     * @param tq           task queue, shared amongst all SIWs and BWs
     */
    public SessionInputWorker(int workerNumber, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue,
                    ArrayBlockingQueue<Integer> tq) {
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
                // wait for a message, validate it, and enqueue it.
                Object obj = in.readObject();
                Message msg = ValidateInput.validateMessage(obj);
                messageQueue.put(msg);

                // operating on the precondition that all worker ID strings follow a format.
                int workerNum = Integer.parseInt(workerID.split("-")[1]);

                // queue up the task for a MessageRouter.
                taskQueue.put(workerNum);

            } catch (IOException e) {
                if (isRunning) {
                    /*
                     * NOTE if we're still running and an IOException pops, something is wrong.
                     * otherwise, we can shutdown gracefully.
                     */
                    System.out.println(workerID + " bad error, please verify. --> " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println(workerID + " Error! --> " + e.getMessage());
            }

            // check to see if it is time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    // vocalize shut down.
                    proclaimShutdown();
                    break;
                }
            }
        }
    }
}
