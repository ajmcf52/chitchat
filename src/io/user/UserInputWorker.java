package io.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;

import io.InputWorker;
import messages.Message;
import misc.ValidateInput;

/**
 * A special type of InputWorker that works solely for ChatUsers by receiving
 * messages from the SessionCoordinator's OutputWorker.
 */
public class UserInputWorker extends InputWorker {

    private Object inNotifier; // to notify UserInputHandler of newly received messages to process.

    /**
     * constructor for UserInputWorker.
     * 
     * @param workerNum        unique number assigned to this worker its class
     * @param input            reader used to read in messages
     * @param msgQueue         where newly received messages are placed
     * @param incomingNotifier to notify UserInputHandler of messages to process.
     */
    public UserInputWorker(int workerNum, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue,
                    Object incomingNotifier) {
        super("UIW-" + Integer.toString(workerNum), input, msgQueue);
        inNotifier = incomingNotifier;
    }

    /**
     * getter for UserInputWorker's notifier object.
     * 
     * @return return UserInputWorker's notifier
     */
    public Object getNotifier() {
        return inNotifier;
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
                // read in a message and validate it.
                obj = in.readObject();
                msg = ValidateInput.validateMessage(obj);

            } catch (IOException e) {
                /**
                 * operating on the assumption that someone has closed our socket for good
                 * reason, and that it is likely time to turn off and exit.
                 */
                turnOff();
                break;
            } catch (Exception e) {
                System.out.println("UserInputWorker Error! --> " + e.getMessage());
            }

            // enqueue the newly received message.
            messageQueue.add(msg);

            synchronized (inNotifier) {
                inNotifier.notify(); // sends a signal to the InputHandler that there is a message to be
                                     // handled.
            }

            // check to see if it's time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        } // end of while loop
          // vocalize the shut down.
        proclaimShutdown();
    }

}
