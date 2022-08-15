package io;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.PrintWriter;

import misc.Worker;
/**
 * this class is responsible for writing outgoing messages through a particular Socket
 * to a given ChatUser. Outgoing messages are retrieved from a message-oriented ABQ.
 * 
 * NOTE this class is slightly DIFFERENT from its counterpart, InputWorker, in that
 * its defined functionality in its capacity of server ChatUser and SeshCoordinator are
 * EXACTLY the same. Thus, there is zero need to subclass OutputWorker with SOW and COW.
 */
public class OutputWorker extends Worker {
    
    private PrintWriter out; // what will be used to send outgoing messages.
    private ArrayBlockingQueue<String> messageQueue; // where outgoing messages will be retrieved from.
    private Object newMessageNotifier; // wait on this for new messages to be forwarded.
   
    /**
     * constructor of the OutputWorker.
     * @param workerNum number unique to this worker within its worker class.
     * @param output PrintWriter to be used for writing outgoing messages; initialized by SessionCoordinator
     * @param msgQueue initialized by SessionCoordinator, where this thread will retrieve outgoing messages to be sent.
     * @param nmn new message notifier
     */
    public OutputWorker(int workerNum, PrintWriter output, ArrayBlockingQueue<String> msgQueue, Object nmn) {
        super("OW-" + Integer.toString(workerNum));
        messageQueue = msgQueue;
        out = output;
        newMessageNotifier = nmn;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {

        while (true) {
            try {
                newMessageNotifier.wait(); // wait until notified by a BroadcastWorker, then take all that are available.
                ArrayList<String> toSend = new ArrayList<String>();
                messageQueue.drainTo(toSend);
                for (String msg : toSend) {
                    out.write(msg);
                }
                out.flush();
            } catch (Exception e) {
                System.out.println("OutputWorker Error! --> " + e.getMessage());
            }
            
            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }
}
