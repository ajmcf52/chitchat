package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

import net.ChatUser;
import worker.UserSetupWorker;
import misc.Constants;
import misc.PanelNames;
import main.ApplicationState;
import misc.ValidateInput;

/*
 * First panel that is seen upon starting Chatter.
 */
public class LoginPanel extends JPanel {
    private final static int ALIAS_FIELD_WIDTH = 20; // width of alias text field in columns (?)

    private JLabel welcomeLabel;
    private JLabel aliasPrompt;
    private JLabel aliasInstructions;

    private JTextField aliasField;
    private JLabel hitReturnNotice;
    private JLabel badAliasWarning;

    private ChatUser userRef;
    private Object chatUserLock;
    private ApplicationState appState;

    /**
     * Constructor. Basic stuff.
     */
    public LoginPanel(ChatUser ref, Object cuLock, ApplicationState state) {
        userRef = ref; // we pass a ChatUser reference in so it can be passed along to
                       // UserSetupThread's ctor.
        chatUserLock = cuLock; // this will be passed on to UserSetupThread, so it can notify once ChatUser has
                               // been instantiated.
        appState = state;

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

        aliasField = new JTextField(ALIAS_FIELD_WIDTH);
        aliasField.setVisible(true);
        aliasField.setAlignmentX(Component.CENTER_ALIGNMENT);
        aliasField.setFont(new Font("Serif", Font.PLAIN, 18));
        aliasField.setMaximumSize(aliasField.getPreferredSize()); // stops text field from needlessly expanding in
                                                                  // BoxLayout

        hitReturnNotice = new JLabel();
        hitReturnNotice.setText("Press \"Return\" when done.");
        hitReturnNotice.setFont(new Font("Serif", Font.PLAIN, 14));
        hitReturnNotice.setVisible(true);
        hitReturnNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        badAliasWarning = new JLabel();
        badAliasWarning.setText("Alias must contain only letters and numbers & be between 2-16 characters long!");
        badAliasWarning.setForeground(Color.RED);
        badAliasWarning.setFont(new Font("Serif", Font.PLAIN, 14));
        badAliasWarning.setVisible(false);
        badAliasWarning.setAlignmentX(Component.CENTER_ALIGNMENT);

        // setting up the box layout.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(welcomeLabel);
        this.add(aliasPrompt);
        this.add(aliasInstructions);
        this.add(aliasField);
        this.add(hitReturnNotice);
        this.add(badAliasWarning);
        this.setName(PanelNames.LOGIN_PANEL);

        // setting up event listener for when users press "Return".

        /**
         * main function here is keyReleased(e), which is responsible for creating new
         * users (given that the entered alias is valid)
         */
        aliasField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {

                /**
                 * We only want to process KeyEvents originated via the "Return" key.
                 */
                if (e.getKeyCode() != Constants.KC_RETURN) {
                    return;
                }

                // 1. retrieve text String from inside the text field.
                JTextField src = (JTextField) e.getSource();
                String chatUserAlias = src.getText();

                // verify input with two checks: one for length, one for alphanumeric.

                if (!ValidateInput.validateLength(chatUserAlias, Constants.MIN_USER_INPUT_LENGTH,
                                Constants.MAX_USER_INPUT_LENGTH)
                                || !ValidateInput.validateAlphaNumeric(chatUserAlias)) {
                    triggerErrorMessage(badAliasWarning);
                    return;
                }

                /**
                 * this worker sets up the ChatUser.
                 * 
                 * Alias uniqueness is maintained by the structure of usernames, combining
                 * aliases and user ID numbers provided by the Registry. See ChatUser.java for
                 * more details.
                 */
                UserSetupWorker usWorker = new UserSetupWorker(0, chatUserAlias, userRef, chatUserLock, appState);
                usWorker.start();

            }
        });
    }

    /**
     * this method is called when a bad alias is entered into the alias field
     * textbox for a new User. Warning appears for 5 seconds.
     * 
     * @param badAliasWarning JLabel containing the warning text.
     */
    public void triggerErrorMessage(JLabel badAliasWarning) {
        badAliasWarning.setVisible(true);
        Timer timer = new Timer(5000, event -> {
            badAliasWarning.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }

}
