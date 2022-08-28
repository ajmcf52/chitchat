package io.user;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import messages.Message;
import ui.ChatWindow;

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
    private ArrayBlockingQueue<Message> messageQueue;

    private volatile boolean isRunning; // flag is used to switch worker on and off.
    private Object runLock; //lock for aforementioned run flag.
    private Object incomingMessageNotifier; // waited on for new messages to pull from the queue.

    /**
     * constructor for UIH.
     * @param chatWin chat window reference object
     * @param msgQueue message queue
     * @param incomingNotifier new message notifier, notified by UserInputWorker (the receiver of messages via Socket)
     */
    public UserInputHandler(ChatWindow chatWin, ArrayBlockingQueue<Message> msgQueue, Object incomingNotifier) {
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
            ArrayList<Message> messages = new ArrayList<>();
            try {
                synchronized (incomingMessageNotifier) { // notified by UserInputWorker when there is a message to handle
                    incomingMessageNotifier.wait();
                }
                
                messageQueue.drainTo(messages);
                
            } catch (Exception e) {
                System.out.println("UIH Error! --> " + e.getMessage());
            }
            
            // handle each and every single message.
            for (Message msg : messages) {
                handleMessage(msg);
            }

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
     * @param msg the incoming message.
     */
    public void handleMessage(Message msg) {

        // NOTE we only want to receive this message in cases where we didn't send it!
        if (chatWindowRef.getAssociatedAlias() != msg.getAssociatedSenderAlias()) {
            /**
            * the way we have set up the Message class, the text we would want to display
            * for all possible Message types that a ChatUser can possibly receive will be
            * directly attainable by calling the getContent() method.
            */
            
            chatWindowRef.addLineToFeed(msg.getContent());
        }

        
        
        
        
        
    }
}
