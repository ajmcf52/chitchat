package worker;

import java.util.concurrent.ArrayBlockingQueue;
import java.net.Socket;
import java.util.LinkedList;
import java.io.PrintWriter;

import main.Constants;

/**
 * this class is responsible for writing outgoing messages through a particular Socket
 * to a given ChatUser. Outgoing messages are retrieved from a message-oriented ABQ.
 */
public class OutputWorker extends Thread {
    
    private PrintWriter out; // what will be used to send outgoing messages.
    private ArrayBlockingQueue<String> messageQueue; // where outgoing messages will be retrieved from.
    /**
     * the flag below is used to signify whether this worker is running or not. Can be flipped off from outside
     * this worker's scope using the switchOff() method. From there, the worker thread dies and must be restarted.
     */
    private volatile boolean isRunning;
    private Object runLock; // lock used to check or switch the OutputWorker's flag.
   
    /**
     * constructor of the OutputWorker.
     * @param sock socket by which our PrintWriter will be initialized.
     */
    public OutputWorker(Socket sock) {
        runLock = new Object();
        isRunning = false;
        messageQueue = new ArrayBlockingQueue<String>(Constants.MSG_QUEUE_LENGTH);
        try {
            out = new PrintWriter(sock.getOutputStream());
        } catch (Exception e) {
            System.out.println("OutputWorker Constructor Error! --> " + e.getMessage());
        }
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {

        synchronized (runLock) {
            isRunning = true; // we will check this flag at the end of the while loop.
        }
        while (true) {
            try {
                // blocking operation.
                String msg = messageQueue.take();
                out.write(msg);
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

    /**
     * this method is used to signal to the thread that it is time to exit.
     * Will typically be accessed from outside this thread's scope. 
     */
    public void switchOff() {
        synchronized (runLock) {
            isRunning = false;
        }
    }
}
