package main;

import javax.swing.JPanel;

import ui.MainWindow;
import ui.LoginPanel;
import ui.ChoicePanel;
import net.ChatUser;
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
        ChatUser user = new ChatUser();
        Object chatUserLock = new Object();

        // create all of our application panels.
        LoginPanel loginPanel = new LoginPanel(user, chatUserLock);
        ChoicePanel choicePanel = new ChoicePanel();
        int numPanels = 2;
        JPanel[] appPanels = new JPanel[numPanels];
        appPanels[0] = loginPanel;
        appPanels[1] = choicePanel;

        // create the window
        MainWindow mainWindow = new MainWindow(appPanels);
        try {
            synchronized (chatUserLock) {
                chatUserLock.wait();
            }
        }
        catch (InterruptedException e) {
            System.out.println("ChatterApp error! --> " + e.getMessage());
        }
        System.out.println("ChatUser alias -->" + user.getAlias());
        try {
            Thread.sleep(2500);
        }
        catch (InterruptedException e) {
            System.out.println("ChatterApp thread snooze interrupted between LP and CP");
        }
        mainWindow.showChoicePanel();
    }
}