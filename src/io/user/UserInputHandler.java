package io.user;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import main.AppStateValue;
import main.ApplicationState;
import messages.ExitNotifyMessage;
import messages.ExitRoomMessage;
import messages.JoinNotifyMessage;
import messages.Message;
import messages.WelcomeMessage;
import misc.Worker;
import ui.ChatWindow;

/**
 * This class is responsible for handling newly received messages for the
 * ChatUser.
 */
public class UserInputHandler extends Worker {

    private ChatWindow chatWindowRef; // used to carry out received message actions.
    private ArrayBlockingQueue<Message> messageQueue; // for pulling in new messages.
    private Object inNotifier; // waited on for new messages to pull from the queue.
    private Object mainNotifier; // used to notify the main() subroutine of application state changes.
    private ApplicationState appState; // state of the application.

    /**
     * constructor for UIH.
     * 
     * @param workerNum routing number that associates this worker with a user
     * @param chatWin   chat window reference object
     * @param msgQueue  message queue
     * @param inNotif   notified by UserInputWorker (receiver of messages)
     * @param mainNotif to notify main() of state changes.
     * @param state     state of the application
     */
    public UserInputHandler(int workerNum, ChatWindow chatWin, ArrayBlockingQueue<Message> msgQueue, Object inNotif,
                    Object mainNotif, ApplicationState state) {
        super("UIH-" + Integer.toString(workerNum));
        chatWindowRef = chatWin;
        messageQueue = msgQueue;
        inNotifier = inNotif;
        mainNotifier = mainNotif;
        appState = state;
        isRunning = false;
    }

    /**
     * this worker's main line of execution.
     */
    public void run() {
        turnOn();
        ArrayList<Message> messages = new ArrayList<>();

        while (isRunning) {

            // no need to wait if we have messages to process.
            if (messageQueue.size() == 0) {
                try {
                    synchronized (inNotifier) {
                        inNotifier.wait(); // notified by UserInputWorker
                    }
                } catch (InterruptedException e) {
                    if (isRunning) {
                        System.out.println(workerID + " --> bad shutdown! Investigation needed.");
                        turnOff();
                    }

                } catch (Exception e) {
                    System.out.println("UIH Error! --> " + e.getMessage());
                }
            }
            messageQueue.drainTo(messages);

            // handle each and every single message.
            for (Message msg : messages) {
                handleMessage(msg);
            }
            messages.clear();
            // check if it is time to exit.
            synchronized (runLock) {
                if (!isRunning) {
                    proclaimShutdown();
                    break;
                }
            }
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
                System.out.println("existing participant --> " + p);
            }
            chatWindowRef.addParticipantName(wm.getAssociatedReceivingAlias());

        } else if (msg instanceof JoinNotifyMessage) {
            JoinNotifyMessage jnm = (JoinNotifyMessage) msg;
            chatWindowRef.addParticipantName(jnm.getUserJoined());

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

            synchronized (mainNotifier) {
                mainNotifier.notify(); // This tells main() that it can proceed to the next state.
            }
            return; // NOTE the one message type where we don't add line to feed.

        } else if (msg instanceof ExitNotifyMessage) {
            ExitNotifyMessage enm = (ExitNotifyMessage) msg;
            String userLeaving = enm.getUserLeaving();
            chatWindowRef.removeParticipantName(userLeaving);
        }

        chatWindowRef.addLineToFeed(msg.getContent());
    }
}
