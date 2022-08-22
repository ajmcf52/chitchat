package io.user;

import javax.swing.SwingUtilities;

import misc.Constants;
import misc.TimeStampGenerator;
import net.ChatUser;
import ui.ChatWindow;

/**
 * this class is responsible for retrieving passing along user-supplied
 * information (i.e., text messages, exit events, etc) to the user's OutputWorker.
     */
public class UserOutputHandler extends Thread {

    private ChatUser chatUser; // reference object for triggering the sending messages.
    private ChatWindow chatWindowRef; // reference window for updating chat feed.
    private Object eventNotifier; // UOH waits on this for various events to pop up for it to handle
    private volatile boolean isRunning; // flag used to signal a shut down
    private final Object runLock = new Object(); // lock object used to externally signal a shut down while avoiding race conditions

    /**
     * constructor for UOH.
     * @param user reference to the user attached to the chat window.
     * @param notifier object used to notify this worker of events needing to be handled.
    */
    public UserOutputHandler(Object notifier, ChatUser user, ChatWindow chatWindow) {
        chatUser = user;
        chatWindowRef = chatWindow;
        eventNotifier = notifier;
        isRunning = false;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        isRunning = true;

        System.out.println("UserOutputHandler has booted; waiting for events...");
        while (isRunning) {
            try {
                synchronized (eventNotifier) {
                    eventNotifier.wait();
                }
            } catch (Exception e) {
                System.out.println("UOH Error! --> " + e.getMessage());
            }
            // UOH has been woken up; check for an event to handle
            String toSend = chatWindowRef.retrieveChatFieldText();
                
            String timestamp = TimeStampGenerator.now();

            // package the message into an acceptable format, and push it along to the ChatUser's OutputWorker.
            String completeMsg = "[" + timestamp + "]" + Constants.DELIM + chatUser.getAlias() + ":" + Constants.DELIM + toSend + '\n';
            chatUser.pushOutgoingMessage(completeMsg);

            synchronized (runLock) {
                if (!isRunning) {
                    break;
                }
            }
        }
    }


        /**
         * method used to externally signal a shut down to this worker.
         */
    public void turnOff() {
        synchronized (runLock) {
            isRunning = false;
        }
    }
}
