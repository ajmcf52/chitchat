package io.user;

import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.SwingUtilities;

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
    private Object incomingMessageNotifier; // waited on for new messages to pull from the queue.

    /**
     * constructor for UIH.
     * @param chatWin chat window reference object
     * @param msgQueue message queue
     * @param incomingNotifier new message notifier, notified by UserInputWorker (the receiver of messages via Socket)
     */
    public UserInputHandler(ChatWindow chatWin, ArrayBlockingQueue<String> msgQueue, Object incomingNotifier) {
        chatWindowRef = chatWin;
        messageQueue = msgQueue;
        isRunning = false;
        runLock = new Object();
        incomingMessageNotifier = incomingNotifier;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {

        switchOn();

        while (true) {
            String msg = null;
            try {
                synchronized (incomingMessageNotifier) {
                    // wait here until notified by UserInputWorker that we have new messages to process
                    incomingMessageNotifier.wait();
                }
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
        msg.replace(Constants.DELIM," ");
        
        if (components[0].equals(Constants.WELCOME_TAG)) {
            // if we enter in here, we have a welcome message.
            // welcome messages hold, after the first appearance of ':', the room name.
            // we will use this to set the title of the window accordingly.

            /**
             * seeking the first ":" that appears after the first ".", as this will be
             * followed by the room name. We can expect this format to be the same every time,
             * as it is programmed inside SessionCoordinator.
             */
            int firstPeriodIndex = msg.indexOf(".");
            int semiColonIndex = msg.indexOf(":", firstPeriodIndex);

            String roomName = msg.substring(semiColonIndex + 1);
            chatWindowRef.setTitle("Chatter --- " + roomName);
            msg = msg.replace(Constants.WELCOME_TAG, "");
            msg = msg.substring(1);

            /**
             * splitting twice on key points of the welcome string to determine the user alias,
             * so we can add it to the participant list.
             * 
             * There's probably a better way to do this, but this way works as well.
             */
            String[] args = msg.split(", ");
            args = args[1].split("\\.");
            String alias = args[0];
            chatWindowRef.addParticipantName(alias);
            
        }
        final String finalMessage = msg;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                chatWindowRef.addLineToFeed(finalMessage);
            }});
        
        
        
    }
}
