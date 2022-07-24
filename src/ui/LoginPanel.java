package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

import net.ChatUser;
import main.Constants;
/*
 * First panel that is seen upon executing the Chatter app via ChatterApp.java
 */
public class LoginPanel extends JPanel {
    private JLabel welcomeLabel;
    private JLabel aliasPrompt;
    private JLabel aliasInstructions;
    private JTextField aliasField;
    private JLabel hitReturnNotice;
    private JLabel badAliasWarning;

    /**
     * to be instantiated by another Thread-based worker (from within the key event handler) 
     * that will communicate with the Registry.
     */
    private ChatUser userRef;

    /**
     * Constructor. Basic stuff.
     */
    public LoginPanel() {
        welcomeLabel = new JLabel();
        welcomeLabel.setText("Welcome to Chatter!");
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 40));
        welcomeLabel.setVisible(true);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasPrompt = new JLabel();
        aliasPrompt.setText("Please enter an alias below:");
        aliasPrompt.setFont(new Font("Serif", Font.BOLD, 22));
        aliasPrompt.setVisible(true);
        aliasPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasInstructions = new JLabel();
        aliasInstructions.setText("(must contain between 2-16 characters, consisting only of letters and numbers)");
        aliasInstructions.setFont(new Font("Serif", Font.ITALIC, 12));
        aliasInstructions.setVisible(true);
        aliasInstructions.setAlignmentX(Component.CENTER_ALIGNMENT);

        aliasField = new JTextField(20);
        aliasField.setVisible(true);
        aliasField.setAlignmentX(Component.CENTER_ALIGNMENT);
        aliasField.setFont(new Font("Serif", Font.PLAIN, 18));
        aliasField.setMaximumSize(aliasField.getPreferredSize()); // stops text field from needlessly expanding in BoxLayout
        
        hitReturnNotice = new JLabel();
        hitReturnNotice.setText("Press \"Return\" when done.");
        hitReturnNotice.setFont(new Font("Serif", Font.PLAIN, 14));
        hitReturnNotice.setVisible(true);
        hitReturnNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        badAliasWarning = new JLabel();
        badAliasWarning.setText("Alias must contain only letters and numbers & be between 2-16 characters long!");
        badAliasWarning.setForeground(Color.RED);
        badAliasWarning.setFont(new Font("Serif", Font.PLAIN, 14));
        badAliasWarning.setVisible(true);
        badAliasWarning.setAlignmentX(Component.CENTER_ALIGNMENT);

        // setting up the box layout.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(welcomeLabel);
        this.add(aliasPrompt);
        this.add(aliasInstructions);
        this.add(aliasField);
        this.add(hitReturnNotice);
        this.add(badAliasWarning);

        // setting up event listener for when users press "Return".

        aliasField.addKeyListener(new KeyListener(){
            // keyPressed + keyTyped are both included simply to satisfy the compiler.
            public void keyPressed(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}

            /*
             * event handler for keyReleased. this will be the handler that will be used to process
             * the passing of the typed user alias to a Thread in setting up the ChatUser.
             */
            public void keyReleased(KeyEvent e) {

                /**
                 * We only want to process KeyEvents originated via the "Return" key.
                 */
                if (e.getKeyCode() != Constants.KC_RETURN) {
                    return;
                }
                
                // 1. retrieve text String from inside the text field.
                JTextField src = ((JTextField) e.getSource()); // should be aliasField, but we attain this way to satisfy OCD.
                String chatUserAlias = src.getText();
                
                // TODO 2. verify the input (2 <= String.length() <= 16 && each char must be one of [a-z][A-Z][0-9])
                

                // 3a. if input doesn't pass, trigger a Timer and/or Runnable that sets the visibility of 'badAliasWarning' to true,
                // waits 4 seconds, then sets visibility to false again.

                // 3b. if input is good, pass the input to a Runnable that does the following:
                /**
                 * I) messages the Registry socket, requesting a UID.
                 * II) receive said UID from the registry, and use that along with the alias to instantiate a ChatUser.
                 * It is likely that this Runnable will be instantiated as a custom class implementing the Runnable interface;
                 * an instance of this class will be created within the scope of ChatterApp.java, though it will not be started
                 * until it is required to perform the critical task.
                 */
            }
        });
    }

    /**
     * This class is responsible for setting up the ChatUser with its properties, namely its alias and UID.
     */
    public class UserSetupThread extends Thread {
        
        /**
         * the UserSetupThread's main course of action.
         */
        public void run() {
            Socket socket = new Socket()
        }
    }
}
