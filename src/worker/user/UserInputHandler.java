package worker.user;

import java.util.concurrent.ArrayBlockingQueue;

import ui.ChatWindow;
import misc.Constants;

/**
 * This class is responsible for handling newly received
 * messages for the ChatUser.
 */
public class UserInputHandler extends Thread {
    
    /**
     * In place so this user can push incoming/outgoing messages into the chat feed.
     * 
     * Another reason will be also to define the ActionListener for pushing new 
     * messages to SessionCoordinator within this class.
     */
    private ChatWindow chatWindowRef;

    /**
     * this is where incoming messages will be retrieved from,
     * after they are placed there by UserInputWorker.
     */
    private ArrayBlockingQueue<String> messageQueue;

    private volatile boolean isRunning; // flag is used to switch worker on and off.
    private Object runLock; //lock for aforementioned run flag.

    /**
     * constructor for UIH.
     * @param chatWin chat window reference object
     * @param msgQueue message queue
     */
    public UserInputHandler(ChatWindow chatWin, ArrayBlockingQueue<String> msgQueue) {
        chatWindowRef = chatWin;
        messageQueue = msgQueue;
        isRunning = false;
        runLock = new Object();
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {

        switchOn();

        while (true) {
            String msg = null;
            try {
                msg = messageQueue.take();
            } catch (Exception e) {
                System.out.println("UIH Error! --> " + e.getMessage());
            }
            handleMessage(msg);

            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }

    /**
     * used to switch on this thread worker.
     */
    public void switchOn() {
        synchronized (runLock) {
            isRunning = true;
        }
    }

    /**
     * used to switch off this thread worker.
     */
    public void switchOff() {
        synchronized (runLock) {
            isRunning = false;
        }
    }

    /**
     * handles incoming messages appropriately.
     * @param msg
     */
    public void handleMessage(String msg) {

        String[] components = msg.split(Constants.DELIM);
        if (msg.startsWith(Constants.SC_TAG)) {
            // if we enter in here, we have a control message (i.e., not from another User)
            msg.replace(Constants.DELIM," ");
            chatWindowRef.addLineToFeed(msg);
        }
    }
}
