package io;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import messages.ExitRoomMessage;
import messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;

import misc.Worker;

/**
 * this class is responsible for writing outgoing messages through a particular
 * Socket to OR from a given ChatUser. Outgoing messages are retrieved from a
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
    private Object outNotifier; // wait on this for new messages that require sending.

    /**
     * constructor of the OutputWorker.
     * 
     * @param workerCode 2-character code unique to this worker within its class.
     * @param output     stream used for writing outgoing messages.
     * @param msgQueue   where outgoing messages to be sent are pulled from.
     * @param outNotif   notified when new messages require sending.
     */
    public OutputWorker(String workerCode, ObjectOutputStream output, ArrayBlockingQueue<Message> msgQueue,
                    Object outNotif) {
        super("OW-" + workerCode);
        messageQueue = msgQueue;
        out = output;
        outNotifier = outNotif;
    }

    /**
     * getter for OW's notifier Object.
     * 
     * @return notifier object for OutputWorker
     */
    public Object getNotifier() {
        return outNotifier;
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
                    synchronized (outNotifier) {
                        outNotifier.wait();
                    }
                }

                for (Object msg : toSend) {
                    out.writeObject(msg);
                    // if we catch an ERM as it's going out, we know to turn off.
                    if (msg instanceof ExitRoomMessage) {
                        turnOff();
                        break;
                    }
                }
                out.flush();
            } catch (IOException e) {
                synchronized (runLock) {
                    if (isRunning) {
                        isRunning = false;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("OutputWorker Error! --> " + e.getMessage());
            }

            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        } // end of while loop

        proclaimShutdown();
    }

    /**
     * triggers the sending of a message.
     * 
     * @param msg message to be sent
     */
    public void triggerMessageSend(Message msg) {
        try {
            messageQueue.put(msg);
            synchronized (outNotifier) {
                outNotifier.notify();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while pushing into " + workerID + "'s outgoing queue! --> "
                            + e.getMessage());
        }

    }
}
