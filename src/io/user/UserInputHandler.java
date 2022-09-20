package io.user;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.SwingUtilities;

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
 * This class handles newly received messages for its ChatUser.
 */
public class UserInputHandler extends Worker {

    private ChatWindow chatWindowRef; // used to carry out appropriate message reactions.
    private ArrayBlockingQueue<Message> messageQueue; // for pulling in new messages.
    private Object inNotifier; // waited on for new messages to pull from the queue.
    private Object mainNotifier; // used to notify the main() subroutine of application state changes.
    private ApplicationState appState; // state of the application.

    /**
     * constructor for UserInputHandler.
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
                        inNotifier.wait(); // (notified by UserInputWorker)
                    }
                } catch (InterruptedException e) {
                    if (isRunning) {
                        System.out.println(workerID + " --> bad interrupt! Investigation needed.");
                        turnOff();
                    }

                } catch (Exception e) {
                    System.out.println("UIH Error! --> " + e.getMessage());
                }
            }
            messageQueue.drainTo(messages);

            // handle each message.
            for (Message msg : messages) {
                handleMessage(msg);
            }
            messages.clear();
            // check if it is time to exit, and if so, vocalize it.
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

        if (msg instanceof WelcomeMessage) {
            WelcomeMessage wm = (WelcomeMessage) msg;

            // if the title doesn't end with "UNASSIGNED", we know it to be a duplicate
            // WelcomeMessage and we can rightfully discard it.
            if (!chatWindowRef.getTitle().endsWith("UNASSIGNED")) {
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    chatWindowRef.setTitle("CHATTER --- " + wm.getAssociatedRoomName());
                    /**
                     * this adds all names of people currently in the chat first, then our own at
                     * the end. NOTE if isHosting == true, this works perfect, as getParticipants()
                     * will return an empty list.
                     */
                    for (String p : wm.getParticipants()) {
                        chatWindowRef.addParticipantName(p);
                    }
                    // int participantCount = wm.getParticipants().size();
                    // String lastGuestJoined = participantCount > 0 ?
                    // wm.getParticipants().get(participantCount - 1) : "";
                    // if (lastGuestJoined.equals(anObject))
                    chatWindowRef.addParticipantName(wm.getAssociatedReceivingAlias());
                }
            });

        }
        /*
         * these notify us of another ChatUser joining the session.
         */
        else if (msg instanceof JoinNotifyMessage) {
            JoinNotifyMessage jnm = (JoinNotifyMessage) msg;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    chatWindowRef.addParticipantName(jnm.getUserJoined());
                }
            });

        }
        /**
         * ERMs are always initially sent by ExitRoomWorkers responding to ChatUsers
         * that want to leave their session. Receiving one back indicates that the
         * SessionCoordinator has acknowledged the ERM and that the ChatUser is fine to
         * leave.
         */
        else if (msg instanceof ExitRoomMessage) {

            // leave the "CHATTING" state for the "CHOICE_PANEL" state.
            appState.setAppState(AppStateValue.CHOICE_PANEL);

            synchronized (mainNotifier) {
                mainNotifier.notify(); // This tells main() that it can proceed to the next state.
            }
            return; // NOTE ERMs are the one message where we don't add a line to the feed.

        }
        /**
         * indicates that a ChatUser has left the session we are currently in.
         */
        else if (msg instanceof ExitNotifyMessage) {
            ExitNotifyMessage enm = (ExitNotifyMessage) msg;
            String userLeaving = enm.getUserLeaving();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    chatWindowRef.removeParticipantName(userLeaving);
                }
            });
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chatWindowRef.addLineToFeed(msg.getContent());
            }
        });
    }
}
