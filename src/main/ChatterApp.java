package main;

import ui.MainWindow;
import ui.LoginPanel;
/**
 * main entry point for the chatter application.
 */
public class ChatterApp {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        LoginPanel lPanel = new LoginPanel();
        MainWindow mw = new MainWindow(lPanel);
        // create a window and panel object, fire them up, and let the music play.
    }
}