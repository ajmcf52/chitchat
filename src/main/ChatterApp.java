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
 * main entry point for the chatter application.
 */
public class ChatterApp {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        /**
         * TODO come up with a system that allows a user to input a port number into the program
         * that will be used for the ChatUser.
         * 
         * a. first version, we will declare the port locally here.
         * b. second version, we will make use of command line arguments.
         * c. third (and probably final) version will make use of a config text file, 
         * that will be read to gather the chat client's port number.
         * 
         * Bear in mind, too, that we will want to come up with a system that
         * keeps generating port numbers randomly until agiven port number works
         * for creating the ChatUser's socket. If we iron this out well enough, we
         * may not even need a method of having a user-supplied port number; the 
         * program itself can take care of that.
         * 
         */

        // variables!!
        // Passing in a null ChatUser. 
        // This will eventually be initialized by UserSetupThread via LoginPanel's EvtHandler.
        ApplicationState appState = new ApplicationState();
        ChatUser chatUser = new ChatUser();
        Object chatUserLock = new Object();
        Object chatLeaveNotifier = new Object();
        ChatWindow chatWindow = null;

        // create all of our application panels.
        LoginPanel loginPanel = new LoginPanel(chatUser, chatUserLock, appState);
        ChoicePanel choicePanel = new ChoicePanel(chatUser, chatUserLock, appState);
        RoomSelectPanel roomSelectPanel = new RoomSelectPanel(appState);
        RoomNamePanel roomNamePanel = new RoomNamePanel(chatUser, chatUserLock, appState);

        int numPanels = 4;
        JPanel[] appPanels = new JPanel[numPanels];
        appPanels[0] = loginPanel;
        appPanels[1] = choicePanel;
        appPanels[2] = roomSelectPanel;
        appPanels[3] = roomNamePanel;

        // create the window
        MainWindow mainWindow = new MainWindow(appPanels);
        boolean isRunning = true;

        appState.setAppState(AppStateValue.ROOM_SELECT);

        while (isRunning) {
            /**
             * enter here if we are at the login panel.
             */
            if (appState.getAppState() == AppStateValue.LOGIN_PANEL) {
                try {
                    /*
                     * user will progress past this wait() after
                     * their alias has been entered and set up on
                     * the back-end with the registry.
                     */
                    synchronized (chatUserLock) {
                        chatUserLock.wait();
                    }
                }
                catch (InterruptedException e) {
                    System.out.println("ChatterApp error! --> " + e.getMessage());
                }
                System.out.println("ChatUser alias -->" + chatUser.getAlias());
                try {
                    Thread.sleep(2500);
                }
                catch (InterruptedException e) {
                    System.out.println("ChatterApp thread snooze interrupted between LP and CP");
                }
            }
            /**
             * enter here if we are at the choice panel.
             */
            else if (appState.getAppState() == AppStateValue.CHOICE_PANEL) {
                /**
                 * TODO add a "Back" button on the choice screen,
                 * which allows a person to go back and choose a new
                 * screen name. 
                 */
                mainWindow.showChoicePanel();
                try {
                    /*
                     * user will progress past this wait() after
                     * a button has been pressed on the ChoicePanel.
                     */
                    synchronized (chatUserLock) {
                        chatUserLock.wait();
                    }
                }
                catch (InterruptedException e) {
                    System.out.println("ChatterApp error! --> " + e.getMessage());
                }
                try {
                    Thread.sleep(1250);
                }
                catch (InterruptedException e) {
                    System.out.println("ChatterApp thread snooze interrupted during ChoicePanel");
                }
            }
            /**
             * enter here if we have opened a chat window.
             */
            else if (appState.getAppState() == AppStateValue.CHATTING) {
                // connect to the SeshCoordinator, set up the communication pathways, and begin chatting.
                System.out.println("Chatting");
                chatWindow = new ChatWindow("Unassigned Chat");
                chatWindow.addLineToFeed("Connecting to SessionCoordinator...");
                chatUser.initializeChatRoomRef(chatWindow);
                chatUser.start();

                try {
                    synchronized (chatLeaveNotifier) {
                        chatLeaveNotifier.wait();
                    }
                } catch (Exception e) {
                    System.out.println("ChatterApp Error!--> " + e.getMessage());
                }
            }

            else if (appState.getAppState() == AppStateValue.ROOM_SELECT) {
                mainWindow.showRoomSelectPanel();
                try {
                    chatUserLock.wait();
                } catch (Exception e) {
                    System.out.println("CU Error during ROOM_SELECT --> " + e.getMessage());
                }
            }

            else if (appState.getAppState() == AppStateValue.ROOM_NAMING) {
                mainWindow.showRoomNamingPanel();
                try {
                    chatUserLock.wait();
                } catch (Exception e) {
                    System.out.println("CU Error during ROOM_NAMING --> " + e.getMessage());
                }
            }
        }

    }
}