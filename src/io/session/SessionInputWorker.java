package io.session;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;

import java.util.LinkedList;

/**
 * a special type of InputWorker that solely works with the SessionCoordinator.
 */
public class SessionInputWorker extends InputWorker {

    private Object scNotifier; // notify this to wake up the SessionCoordinator.
    private volatile boolean newMessageFlag; // flip this to indicate that a new message has come in.
    private Object newMessageLock; // lock this before flipping for care against race conditions
    
    /**
     * SIW constructor.
     * @param input stream to read messages from
     * @param msgQueue queue where newly received messages are to be placed
     * @param scLock used to notify SC when new message(s) are ready to be retrieved and forwarded. 
     * @param nmf flipping indicates to SC that new messages have come in.
     * @param nml lock this before flipping for thread safety.
     */
    public SessionInputWorker(BufferedReader input, ArrayBlockingQueue<String> msgQueue, Object scLock, boolean nmf, Object nml) {
        super(input, msgQueue);
        scNotifier = scLock;
        newMessageFlag = nmf;
        newMessageLock = nml;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        // to temporarily hold messages.
        // will only ever be accessed by this worker, therefore no synchronization needed.
        LinkedList<String> messages = new LinkedList<String>(); 
        
        switchOn();

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
                // safely flip the new message flag.
                synchronized (newMessageLock) {
                    newMessageFlag = true;
                }
                // notify SessionCoordinator that new messages have come in.
                scNotifier.notify();

            } catch (Exception e) {
                System.out.println("SessionInputWorker Error! --> " + e.getMessage());
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
