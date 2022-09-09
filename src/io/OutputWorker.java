package io;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import messages.Message;

import java.io.ObjectOutputStream;

import misc.Worker;

/**
 * this class is responsible for writing outgoing messages through a particular
 * Socket to a given ChatUser. Outgoing messages are retrieved from a
 * message-oriented ABQ.
 * 
 * NOTE this class is slightly DIFFERENT from its counterpart, InputWorker, in
 * that its defined functionality in its capacity of server ChatUser and
 * SeshCoordinator are EXACTLY the same. Thus, there is zero need to subclass
 * OutputWorker with SOW and COW.
 */
public class OutputWorker extends Worker {

    private ObjectOutputStream out; // what will be used to send outgoing messages.
    private ArrayBlockingQueue<Message> messageQueue; // where outgoing messages will be retrieved from.
    private Object outgoingMsgNotifier; // wait on this for new messages that require sending.

    /**
     * constructor of the OutputWorker.
     * 
     * @param workerCode       2-character code unique to this worker within its
     *                             worker class.
     * @param output           PrintWriter to be used for writing outgoing messages;
     *                             initialized by SessionCoordinator
     * @param msgQueue         initialized by SessionCoordinator, where this thread
     *                             will retrieve outgoing messages to be sent.
     * @param outgoingNotifier used by ChatUser to notify this class of a new
     *                             message needing to be sent.
     */
    public OutputWorker(String workerCode, ObjectOutputStream output, ArrayBlockingQueue<Message> msgQueue,
                    Object outgoingNotifier) {
        super("OW-" + workerCode);
        messageQueue = msgQueue;
        out = output;
        outgoingMsgNotifier = outgoingNotifier;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        turnOn();

        while (true) {
            try {
                // check the message queue before waiting (messages + notify can fire pre-wait)
                ArrayList<Message> toSend = new ArrayList<Message>();
                while (true) {
                    messageQueue.drainTo(toSend);
                    if (toSend.size() > 0) {
                        break;
                    }
                    synchronized (outgoingMsgNotifier) {
                        outgoingMsgNotifier.wait();
                    }
                }

                for (Object msg : toSend) {
                    out.writeObject(msg);
                }
                out.flush();
            } catch (Exception e) {
                System.out.println("OutputWorker Error! --> " + e.getMessage());
            }

            synchronized (runLock) {
                if (!isRunning) {
                    proclaimShutdown();
                    break;
                }
            }
        }
    }

    /**
     * triggers the sending of a message.
     * 
     * @param msg message to be sent
     */
    public void triggerMessageSend(Message msg) {
        try {
            messageQueue.put(msg);
            synchronized (outgoingMsgNotifier) {
                outgoingMsgNotifier.notify();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while pushing into " + workerID + "'s outgoing queue! --> "
                            + e.getMessage());
        }

    }
}
