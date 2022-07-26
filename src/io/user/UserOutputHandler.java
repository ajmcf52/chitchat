package io.user;

import messages.SimpleMessage;
import misc.Worker;
import net.ChatUser;
import ui.ChatWindow;

/**
 * this class is responsible for passing along user-supplied information (i.e.,
 * SimpleMessages) to the user's OutputWorker.
 */
public class UserOutputHandler extends Worker {

    private ChatUser chatUser; // reference object for triggering the sending messages.
    private ChatWindow chatWindow; // reference window for updating chat feed.
    private Object notifier; // UOH waits on this for various events to pop up for it to handle

    /**
     * constructor for UserOutputHandler.
     * 
     * @param workerNum routing number associated with this worker.
     * @param notif     object used to notify this worker of events to be handled.
     * @param user      reference to the user attached to the chat window.
     * @param chatWin   reference object to the chat window.
     */
    public UserOutputHandler(int workerNum, Object notif, ChatUser user, ChatWindow chatWin) {
        super("UOH-" + Integer.toString(workerNum));
        chatUser = user;
        chatWindow = chatWin;
        notifier = notif;
        isRunning = false;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        turnOn();

        while (isRunning) {
            try {
                synchronized (notifier) {
                    notifier.wait(); // wait for a ChatWindow "Send" event to fire
                }
            } catch (InterruptedException e) {
                if (isRunning) {
                    System.out.println(workerID + " bad interrupt! Investigation needed.");
                    turnOff();
                    break;
                }
            } catch (Exception e) {
                System.out.println("UOH Error while waiting for events.. --> " + e.getMessage());
            }

            // at this point, there should be text in the text field to send.
            String toSend = chatWindow.retrieveChatFieldText();
            if (toSend.equals("")) {
                continue; // empty-string messages aren't sent (this doesn't include " ")
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
        // vocalize the shut down.
        proclaimShutdown();
    }
}
