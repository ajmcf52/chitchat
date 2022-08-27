package io;

import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;

import messages.Message;
import misc.Worker;

/**
 * this class is responsible for reading messages from a given Socket
 * and putting them into a message queue to be picked up by an entity on the other side.
 * Each implementing subclass has its own way of notifying the correct entity on
 * the other side of the message queue, which depends on context.
 */
public abstract class InputWorker extends Worker {
    
    protected ObjectInputStream in; // what will be used to read messages from.
    protected ArrayBlockingQueue<Message> messageQueue; // where newly received messages will be placed.

    /**
     * constructor of the InputWorker.
     * @param wid worker ID string, passed along by the class implementing this one.
     * @param input what will be used to read incoming messages; initialized by SessionCoordinator.
     * @param msgQueue where incoming messages will be placed; initialized by SessionCoordinator.
     */
    public InputWorker(String wid, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue) {
        super(wid);
        isRunning = false;
        messageQueue = msgQueue;
        in = input;
    }
}
