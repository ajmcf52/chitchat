package io.user;

import java.io.BufferedReader;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;

/**
 * A special type of InputWorker that receives messages
 * from the SessionCoordinator, either pushing said content
 * directly into either the chatFeed
 */
public class UserInputWorker extends InputWorker {
    
    private Object newMessageNotifier; // used to notify UIH that there are newly received messages to process

    /**
     * constructor for UIW.
     * @param workerNum unique number assigned to this worker within its class
     * @param input reader used to read in messages
     * @param msgQueue where newly received messages are placed
     * @param nmn used to notify the UserInputHandler when there are newly received messages to process.
     * 
     */
    public UserInputWorker(int workerNum, BufferedReader input, ArrayBlockingQueue<String> msgQueue, Object nmn) {
        super("UIW-" + Integer.toString(workerNum), input,msgQueue);
        newMessageNotifier = nmn;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        
        turnOn();

        while (true) {
            String msg = null;
            try {
                msg = in.readLine();
            } catch (Exception e) {
                System.out.println("UserInputWorker Error! --> " + e.getMessage());
            }
            messageQueue.add(msg);
            newMessageNotifier.notify();


            // check to see if it's time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }

}
