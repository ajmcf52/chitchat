package worker.user;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;

import worker.InputWorker;

/**
 * A special type of InputWorker that receives messages
 * from the SessionCoordinator, either pushing said content
 * directly into either the chatFeed
 */
public class UserInputWorker extends InputWorker {
    

    /**
     * constructor for UIW.
     * @param input reader used to read in messages
     * @param msgQueue where newly received messages are placed
     */
    public UserInputWorker(BufferedReader input, ArrayBlockingQueue<String> msgQueue) {
        super(input,msgQueue);
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        
        switchOn();

        while (true) {
            String msg = null;
            try {
                msg = in.readLine();
            } catch (Exception e) {
                System.out.println("UserInputWorker Error! --> " + e.getMessage());
            }
            messageQueue.add(msg);

            // check to see if it's time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }

}
