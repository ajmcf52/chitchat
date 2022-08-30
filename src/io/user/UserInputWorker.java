package io.user;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;
import messages.Message;
import misc.ValidateInput;

/**
 * A special type of InputWorker that receives messages
 * from the SessionCoordinator, either pushing said content
 * directly into either the chatFeed
 */
public class UserInputWorker extends InputWorker {
    
    private Object incomingMsgNotifier; // used to notify UIH that there are newly received messages to process
    private Socket socket; // for informational purposes.
    /**
     * constructor for UIW.
     * @param workerNum unique number assigned to this worker within its class
     * @param input reader used to read in messages
     * @param msgQueue where newly received messages are placed
     * @param incomingNotifier used to notify the UserInputHandler when there are newly received messages to process.
     * @param s client socket
     */
    public UserInputWorker(int workerNum, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue, Object incomingNotifier, Socket s) {
        super("UIW-" + Integer.toString(workerNum), input, msgQueue);
        incomingMsgNotifier = incomingNotifier;
        socket = s;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        turnOn();

        while (true) {
            Object obj = null;
            Message msg = null;
            try {
                if (workerID.endsWith("2")) {
                    System.out.println();
                }
                    
                obj = in.readObject();
                
                msg = ValidateInput.validateMessage(obj);
            } catch (Exception e) {
                System.out.println("UserInputWorker Error! --> " + e.getMessage());
            }
            messageQueue.add(msg);

            synchronized (incomingMsgNotifier) {
                incomingMsgNotifier.notify(); // sends a signal to the InputHandler that there is a message to be handled.
            }

            // check to see if it's time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }

}
