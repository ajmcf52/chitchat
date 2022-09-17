package io;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import messages.ExitRoomMessage;
import messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;

import misc.Worker;

/**
 * this class represents an entity responsible for writing messages out through
 * a particular Socket to/from a given ChatUser. Before being written to the
 * Socket, outgoing messages are retrieved from an ArrayBlockingQueue, which
 * carries with it embedded protection against race conditions.
 *
 * NOTE this class is slightly different from its counterpart, InputWorker, in
 * that its defined functionality/role it is intended to serve for ChatUser and
 * SessionCoordinator are exactly the same. Thus, there is no need to subclass.
 */
public class OutputWorker extends Worker {

    private ObjectOutputStream out; // what will be used to send outgoing messages.
    private ArrayBlockingQueue<Message> messageQueue; // where outgoing messages will be retrieved from.
    private Object outNotifier; // wait on this for new messages that require sending.

    /**
     * constructor of OutputWorker.
     * 
     * @param workerCode differentiates ChatUser/Coordinator writers.
     * @param output     stream used for writing outgoing messages.
     * @param msgQueue   where to-be-sent messages are first pulled from.
     * @param outNotif   notified on when new messages require sending.
     */
    public OutputWorker(String workerCode, ObjectOutputStream output, ArrayBlockingQueue<Message> msgQueue,
                    Object outNotif) {
        super("OW-" + workerCode);
        messageQueue = msgQueue;
        out = output;
        outNotifier = outNotif;
    }

    /**
     * accessor for OutputWorker's notifier.
     * 
     * @return notifier object for OutputWorker
     */
    public Object getNotifier() {
        return outNotifier;
    }

    /**
     * main line of execution.
     */
    public void run() {
        turnOn();

        while (true) {
            try {
                ArrayList<Message> toSend = new ArrayList<Message>();
                /**
                 * this loop ensures we don't accidentally call wait() when there are already
                 * messages queued & ready to be sent out.
                 */
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
                        out.flush();
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

        // vocalize shut down.
        proclaimShutdown();
    }

    /**
     * Allows external entities to push Messages in for sending by this
     * OutputWorker.
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
