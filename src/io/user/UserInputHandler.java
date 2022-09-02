package io.user;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import main.AppStateValue;
import main.ApplicationState;
import messages.ExitRoomMessage;
import messages.JoinNotifyMessage;
import messages.Message;
import messages.WelcomeMessage;
import ui.ChatWindow;

/**
 * This class is responsible for handling newly received messages for the
 * ChatUser.
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
     * this is where incoming messages will be retrieved from, after they are placed
     * there by UserInputWorker.
     */
    private ArrayBlockingQueue<Message> messageQueue;

    private volatile boolean isRunning; // flag is used to switch worker on and off.
    private Object runLock; // lock for aforementioned run flag.
    private Object incomingMessageNotifier; // waited on for new messages to pull from the queue.
    private Object mainAppNotifier; // used to notify the main() subroutine of application state changes.
    private ApplicationState appState; // state of the application.

    /**
     * constructor for UIH.
     * 
     * @param chatWin          chat window reference object
     * @param msgQueue         message queue
     * @param incomingNotifier new message notifier, notified by UserInputWorker
     *                             (the receiver of messages via Socket)
     * @param mainNotifier     Object used to notify the main() subroutine of a
     *                             significant state change.
     * @param state            state of the application
     */
    public UserInputHandler(ChatWindow chatWin, ArrayBlockingQueue<Message> msgQueue, Object incomingNotifier,
                    Object mainNotifier, ApplicationState state) {
        chatWindowRef = chatWin;
        messageQueue = msgQueue;
        isRunning = false;
        runLock = new Object();
        incomingMessageNotifier = incomingNotifier;
        mainAppNotifier = mainNotifier;
        appState = state;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        switchOn();

        while (true) {
            ArrayList<Message> messages = new ArrayList<>();
            // wait until notified by UserInputWorker that there are messages to handle.
            try {
                synchronized (incomingMessageNotifier) {
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

            // check if it is time to exit.
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
     * 
     * @param msg the incoming message.
     */
    public void handleMessage(Message msg) {
        System.out.println();
        /*
         * NOTE we only want to receive this message in cases where: a) we didn't send
         * it b) it's the first WelcomeMessage we've received (duplicates have been
         * noted to infrequently occur)
         */
        if (msg instanceof WelcomeMessage) {
            WelcomeMessage wm = (WelcomeMessage) msg;
            if (!chatWindowRef.getTitle().endsWith("UNASSIGNED")) {
                return;
            }
            chatWindowRef.setTitle("CHATTER --- " + wm.getAssociatedRoomName());
            /**
             * this adds all names of people currently in the chat first, then our own at
             * the end. NOTE if isHosting == true, this works perfect, as getParticipants()
             * will return an empty list.
             */
            for (String p : wm.getParticipants()) {
                chatWindowRef.addParticipantName(p);
            }
            chatWindowRef.addParticipantName(wm.getAssociatedReceivingAlias());
            chatWindowRef.addLineToFeed(wm.getContent());

        } else if (msg instanceof JoinNotifyMessage) {
            JoinNotifyMessage jnm = (JoinNotifyMessage) msg;
            chatWindowRef.addParticipantName(jnm.getUserJoined());
            chatWindowRef.addLineToFeed(jnm.getContent());

        } else if (msg instanceof ExitRoomMessage) {
            /**
             * Recognizing that ERMs are initially always sent by ExitRoomWorkers acting on
             * behalf of ChatUsers that want to leave their session, receiving one back
             * indicates that the SessionCoordinator acknowledges the ERM, and that the
             * ChatUser is fine to leave.
             *
             * In terms of processing the message, there really isn't anything that we
             * actually have to do with it (at least in this current version).
             */
            appState.setAppState(AppStateValue.CHOICE_PANEL);

            synchronized (mainAppNotifier) {
                mainAppNotifier.notify(); // This tells main() that it can proceed to the next state.
            }
        }

        chatWindowRef.addLineToFeed(msg.getContent());
    }
}
