package worker;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * this class is responsible for reading messages from a given Socket
 * and putting them into a message queue to be picked up by an entity on the other side.
 * Each implementing subclass has its own way of notifying the correct entity on
 * the other side of the message queue, which depends on context.
 */
public abstract class InputWorker extends Thread {
    
    protected BufferedReader in; // what will be used to read messages from.
    protected ArrayBlockingQueue<String> messageQueue; // where newly received messages will be placed.
    /**
     * the flag below is used to signify whether this worker is running or not. Can be flipped off from outside
     * this worker's scope using the switchOff() method. From there, the worker thread dies and must be restarted.
     */
    protected volatile boolean isRunning;
    protected Object runLock; // lock used to check or switch the InputWorker's flag.
     
    /**
     * constructor of the InputWorker.
     * @param input what will be used to read incoming messages; initialized by SessionCoordinator.
     * @param msgQueue where incoming messages will be placed; initialized by SessionCoordinator.
     */
    public InputWorker(BufferedReader input, ArrayBlockingQueue<String> msgQueue) {
        runLock = new Object();
        isRunning = false;
        messageQueue = msgQueue;
        in = input;
    }

    /**
     * used to signify that this thread worker has been turned on.
     */
    public void switchOn() {
        synchronized (runLock) {
            isRunning = true;
        }
    }

    /**
     * this method is used to signal the thread that it is time to exit.
     */
    public void switchOff() {
        synchronized (runLock) {
            isRunning = false;
        }
    }

}
