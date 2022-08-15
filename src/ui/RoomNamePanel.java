package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import net.ChatUser;
import main.ApplicationState;
import main.AppStateValue;
import misc.Constants;
import misc.PanelNames;
import misc.ValidateInput;
import requests.RoomSetupWorker;

/**
 * this class represents a panel that allows
 * a user to enter their desired room name
 */
public class RoomNamePanel extends JPanel {

    private JTextField roomNameField; // text field for entering room name
    private JLabel badNameWarning; // warning that pops up when a bad room name is entered
    private JLabel prompt; // prompt text for entering room name
    private JLabel instructions; // instructions for entering a room name
    private JButton okButton; // pressing this allows user to proceed in creating a room
    private JButton backButton; // pressing this returns user to previous panel

    private ChatUser userRef; // reference object to the chat user
    private Object chatUserLock; // used to notify the user following crucial actions
    private ApplicationState appState; // state of the application

    /**
     * constructor for RNP
     * @param ref reference to the user object
     * @param userLock lock that will be used to notify the user
     * @param state application state
     */
    public RoomNamePanel(ChatUser ref, Object userLock, ApplicationState state) {
        userRef = ref;
        chatUserLock = userLock;
        appState = state;

        roomNameField = new JTextField(20); // makes a field with 20 "columns" (i.e., horizontal spaces roughly)
        roomNameField.setVisible(true);
        roomNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomNameField.setFont(new Font("Serif", Font.PLAIN, 18));
        roomNameField.setMaximumSize(roomNameField.getPreferredSize());

        badNameWarning = new JLabel("Name must contain only letters/numbers & be 2-16 characters long!");
        badNameWarning.setForeground(Color.RED);
        badNameWarning.setFont(new Font("Serif", Font.PLAIN, 14));
        badNameWarning.setVisible(false);
        badNameWarning.setAlignmentX(Component.CENTER_ALIGNMENT);

        prompt = new JLabel("Please enter a room name:");
        prompt.setFont(new Font("Serif", Font.BOLD, 22));
        prompt.setVisible(true);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        instructions = new JLabel("must contain between 2-16 characters, containing only letters & numbers.\nNo profanity please! :)");
        instructions.setFont(new Font("Serif", Font.ITALIC, 12));
        instructions.setVisible(true);
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton = new JButton();
        okButton.setText("OK");
        okButton.setFont(new Font("Serif", Font.BOLD, 24));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton = new JButton();
        backButton.setText("Back");
        backButton.setFont(new Font("Serif", Font.BOLD, 24));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // layout programming

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        /**
         * will add items on a top down, left-to-right manner visually.
         */

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.gridheight = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0,0,10,0);
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        this.add(prompt, constraints);

        constraints.gridy = 3;
        constraints.gridheight = 1;
        this.add(instructions, constraints);

        constraints.gridy = 4;
        constraints.insets = new Insets(0,0,0,0);
        this.add(roomNameField, constraints);

        constraints.gridy = 5;
        constraints.insets = new Insets(5,0,10,0);
        this.add(badNameWarning, constraints);

        constraints.gridy = 6;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.insets = new Insets(5, 0, 10, 0);
        this.add(okButton, constraints);

        constraints.gridx = 3;
        this.add(backButton, constraints);

        this.setName(PanelNames.ROOM_NAME_PANEL);

        okButton.addActionListener(e -> {
            // validate input

            String roomName = roomNameField.getText();

            if (!ValidateInput.validateLength(roomName, Constants.MIN_USER_INPUT_LENGTH, Constants.MAX_USER_INPUT_LENGTH)
            || !ValidateInput.validateAlphaNumeric(roomName)) {
                triggerErrorMessage(badNameWarning);
                return;
            }

            RoomSetupWorker rsWorker = new RoomSetupWorker(0, userRef, chatUserLock, appState);
            rsWorker.start();

        });

        backButton.addActionListener(e -> {
            appState.setAppState(AppStateValue.CHOICE_PANEL);
            chatUserLock.notify();
        });

        roomNameField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {
                // only process events if they originate from the "Enter" key
                if (e.getKeyCode() != Constants.KC_RETURN) {
                    return;
                }

                okButton.doClick();
            }

        });
    }

    /**
     * this method is called when a bad room name is entered into the room name field textbox for a new chat room.
     * Warning appears for 5 seconds.
     * @param badNameWarning JLabel containing the warning text.
     */
    public void triggerErrorMessage(JLabel badNameWarning) {
        //System.out.println("farts");
        badNameWarning.setVisible(true);
        Timer timer = new Timer(5000, event -> {
            badNameWarning.setVisible(false);
        });
        timer.setRepeats(false);
        timer.start();
    }
}
