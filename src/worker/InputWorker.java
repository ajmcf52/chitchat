package worker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.net.Socket;
import java.util.LinkedList;

import main.Constants;

/**
 * this class is responsible for reading messages from a given Socket
 * and putting them into a message queue to be picked up by the SessionCoordinator.
 * Upon placing each message into the queue, the IW is then expected to notify the SC.
 */
public class InputWorker extends Thread {
    
    private BufferedReader in; // what will be used to read messages from.
    private ArrayBlockingQueue<String> messageQueue; // where newly received messages will be placed.
    /**
     * the flag below is used to signify whether this worker is running or not. Can be flipped off from outside
     * this worker's scope using the switchOff() method. From there, the worker thread dies and must be restarted.
     */
     private volatile boolean isRunning;
     private Object runLock; // lock used to check or switch the InputWorker's flag.
     private Object newMessageNotifier; // notify this to let the SessionCoordinator know that new messages have come in.

    /**
     * constructor of the InputWorker.
     * @param sock Socket by which our buffered reader will be constructed.
     */
    public InputWorker(Socket sock) {
        runLock = new Object();
        newMessageNotifier = new Object();
        isRunning = false;
        messageQueue = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH,true);
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (Exception e) {
            System.out.println("InputWorker Constructor Error!! -->" + e.getMessage());
        }
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        // to temporarily hold messages.
        // will only ever be accessed by this worker, therefore no synchronization needed.
        LinkedList<String> messages = new LinkedList<String>(); 

        synchronized (runLock) {
            isRunning = true; // we will check this flag at the end of the while loop.
        }
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
                // notify SessionCoordinator that new messages have come in.
                newMessageNotifier.notify();

            } catch (Exception e) {
                System.out.println("InputWorker Error! --> " + e.getMessage());
            }

            // check to see if it is time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
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
