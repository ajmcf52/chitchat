package main;

import javax.swing.JPanel;

import ui.MainWindow;
import ui.room_select.RoomSelectPanel;
import ui.LoginPanel;
import ui.ChoicePanel;
import net.ChatUser;
import ui.ChatWindow;
import ui.RoomNamePanel;

/**
 * This class contains the main entry point for Chatter.
 */
public class ChatterApp {

    public static void main(String[] args) {

        /** --VARIABLE DECLARATIONS-- **/

        // used to control the state machine within main().
        ApplicationState appState = new ApplicationState();

        /**
         * used by various entities in tandem with ChatUser facilitate synchronized
         * comms.
         */
        Object chatUserLock = new Object();

        // ibid, but instead entities use this to communicate with main().
        Object mainAppNotifier = new Object();

        // main controller class of user comms.
        ChatUser chatUser = new ChatUser(chatUserLock, mainAppNotifier, appState);

        // self-explanatory. Instantiated within "CHATTING" state.
        ChatWindow chatWindow = null;

        // -- application panels --

        // screen A: where users enter a screen name (i.e., alias)
        LoginPanel loginPanel = new LoginPanel(chatUser, chatUserLock, appState);

        // screen B: where users choose to create or join a room.
        ChoicePanel choicePanel = new ChoicePanel(chatUser, chatUserLock, appState);

        // screen C1: where users look at and join existing rooms.
        RoomSelectPanel roomSelectPanel = new RoomSelectPanel(appState, chatUser, mainAppNotifier);

        // screen C2: where a user creating a room enters a name for it.
        RoomNamePanel roomNamePanel = new RoomNamePanel(chatUser, chatUserLock, appState);

        // setting up an array of JPanels for CardLayout.
        int numPanels = 4;
        JPanel[] appPanels = new JPanel[numPanels];
        appPanels[0] = loginPanel;
        appPanels[1] = choicePanel;
        appPanels[2] = roomSelectPanel;
        appPanels[3] = roomNamePanel;

        MainWindow mainWindow = new MainWindow(appPanels);
        boolean isRunning = true;

        while (isRunning) {

            if (appState.getAppState() == AppStateValue.LOGIN_PANEL) {
                /**
                 * NOTE there is no showLoginPanel() method in MainWindow (yet), as LoginPanel
                 * is the default 1st panel and we have yet to support name changing. This may
                 * or may not change in the future.
                 */
                try {
                    synchronized (chatUserLock) {
                        chatUserLock.wait(); // notified by UserSetupWorker.
                    }
                } catch (InterruptedException e) {
                    System.out.println("ChatterApp error! --> " + e.getMessage());
                }

                System.out.println("ChatUser alias --> " + chatUser.getAlias());
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    System.out.println("ChatterApp thread snooze interrupted between Login/Choice Panel.");
                }
            }

            else if (appState.getAppState() == AppStateValue.CHOICE_PANEL) {
                /**
                 * TODO add a "Back" button on the choice screen, which allows a person to go
                 * back and choose a new screen name.
                 */
                mainWindow.showChoicePanel();
                try {
                    /**
                     * notified by 1 of 2 action listeners in ChoicePanel.
                     */
                    synchronized (chatUserLock) {
                        chatUserLock.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("ChatterApp error! --> " + e.getMessage());
                }
            }
            /**
             * enter here if we have opened a chat window.
             */
            else if (appState.getAppState() == AppStateValue.CHATTING) {
                // connect to the SeshCoordinator, set up comm pathways, and begin chatting.
                System.out.println("Chatting");
                mainWindow.setVisible(false);
                chatWindow = new ChatWindow("UNASSIGNED", chatUser);
                chatWindow.addLineToFeed("Connecting to SessionCoordinator...");
                chatUser.initializeChatRoomRef(chatWindow);

                chatUser.start();

                try { // main() waits here for an exit notification (delivered by UserInputHandler)
                    synchronized (mainAppNotifier) {
                        mainAppNotifier.wait();
                    }
                } catch (Exception e) {
                    System.out.println("ChatterApp Error!--> " + e.getMessage());
                }
                /**
                 * getting here means we have exited a room.
                 */
                chatWindow.setVisible(false);
                mainWindow.setVisible(true);

                chatWindow.shutDown();
                chatWindow.dispose();

                // notify ChatUser that they are safe to exit their state of Chatting.
                synchronized (chatUserLock) {
                    chatUserLock.notify();
                }
            }

            else if (appState.getAppState() == AppStateValue.ROOM_SELECT) {
                mainWindow.showRoomSelectPanel(roomSelectPanel);
                try {
                    synchronized (mainAppNotifier) {
                        mainAppNotifier.wait();
                    }
                } catch (Exception e) {
                    System.out.println("CU Error during ROOM_SELECT --> " + e.getMessage());
                }
            }

            else if (appState.getAppState() == AppStateValue.ROOM_NAMING) {
                mainWindow.showRoomNamingPanel();
                try {
                    synchronized (chatUserLock) {
                        chatUserLock.wait();
                    }
                    System.out.println("notified from waiting.");
                } catch (Exception e) {
                    System.out.println("CU Error during ROOM_NAMING --> " + e.getMessage());
                }
            }
        }

    }
}