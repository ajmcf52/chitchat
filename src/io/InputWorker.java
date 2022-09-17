package io;

import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;

import messages.Message;
import misc.Worker;

/**
 * this abstract class represents an entity that reads Messages from a given
 * Socket and places them into a given queue, implying that another entity on
 * the other side of the queue is there to pick & handle it.
 *
 * Implementing subclasses have their own ways of notifying the entity on the
 * other side of the message queue, depending on situational context.
 */
public abstract class InputWorker extends Worker {

    protected ObjectInputStream in; // used to read Message objects.
    protected ArrayBlockingQueue<Message> messageQueue; // where newly received messages are placed.

    /**
     * constructor of InputWorker.
     * 
     * @param wid      worker ID string.
     * @param input    incoming message stream.
     * @param msgQueue message queue.
     */
    public InputWorker(String wid, ObjectInputStream input, ArrayBlockingQueue<Message> msgQueue) {
        super(wid);
        isRunning = false;
        messageQueue = msgQueue;
        in = input;
    }
}
