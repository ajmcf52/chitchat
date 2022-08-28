package io.user;

import messages.SimpleMessage;
import net.ChatUser;
import ui.ChatWindow;

/**
 * this class is responsible for retrieving passing along user-supplied
 * information (i.e., SimpleMessages) to the user's OutputWorker.
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
                    eventNotifier.wait(); // wait for a ChatWindow "Send" event to fire
                }
            } catch (Exception e) {
                System.out.println("UOH Error while waiting for events.. --> " + e.getMessage());
            }

            // at this point, there should be text in the text field to send.
            String toSend = chatWindowRef.retrieveChatFieldText();
            if (toSend.equals("")) {
                System.out.println("Blank message... Nothing to send.");
                continue; // if the message is blank, there is nothing to do here.
            }

            // package the text into a message, and push it out.
            SimpleMessage outgoing = new SimpleMessage(chatUser.getAlias(), toSend);
            chatUser.pushOutgoingMessage(outgoing);

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
