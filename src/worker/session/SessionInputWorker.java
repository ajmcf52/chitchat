package worker.session;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.LinkedList;

import worker.InputWorker;

/**
 * a special type of InputWorker that solely works with the SessionCoordinator.
 */
public class SessionInputWorker extends InputWorker {

    private Object newMessageNotifier; // notify this to let the SessionCoordinator know that new messages have come in.
    
    public SessionInputWorker(BufferedReader input, ArrayBlockingQueue<String> msgQueue) {
        super(input, msgQueue);
        newMessageNotifier = new Object();
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
                // notify SessionCoordinator that new messages have come in.
                newMessageNotifier.notify();

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
