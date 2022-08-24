package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;

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

    private final static int ROOM_NAMING_FIELD_WIDTH = 20;
    private final static String WARNING_TEXT = "Name may not include any special characters & can be 2-16 characters long!";

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

        //fieldPanel = new JPanel();

        roomNameField = new JTextField(ROOM_NAMING_FIELD_WIDTH); // makes a field with 20 "columns" (i.e., horizontal spaces roughly)
        roomNameField.setVisible(true);
        roomNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomNameField.setFont(new Font("Serif", Font.PLAIN, 18));
        roomNameField.setMaximumSize(roomNameField.getPreferredSize());

        badNameWarning = new JLabel();
        badNameWarning.setForeground(Color.RED);
        badNameWarning.setText(" ");
        badNameWarning.setFont(new Font("Serif", Font.PLAIN, 14));
        badNameWarning.setVisible(true);
        badNameWarning.setAlignmentX(Component.CENTER_ALIGNMENT);
        badNameWarning.setSize(badNameWarning.getPreferredSize());

        // warningPanel = new JPanel();
        // warningPanel.setLayout(new BorderLayout());
        // warningPanel.setSize(warningPanel.getPreferredSize());
        // warningPanel.add(badNameWarning, BorderLayout.CENTER);

        prompt = new JLabel("Please enter a room name:");
        prompt.setFont(new Font("Serif", Font.BOLD, 22));
        prompt.setVisible(true);

        instructions = new JLabel("<html><center>must contain between 2-16 characters.<br/>No profanity please! :)<center/></html>");
        instructions.setFont(new Font("Serif", Font.ITALIC, 12));
        instructions.setVisible(true);

        okButton = new JButton();
        okButton.setText(" OK ");
        okButton.setFont(new Font("Serif", Font.BOLD, 24));

        backButton = new JButton();
        backButton.setText("Back");
        backButton.setFont(new Font("Serif", Font.BOLD, 24));
        //backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // layout programming

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        /**
         * will add items on a top down, left-to-right manner visually.
         */

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0,0,10,0);
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        this.add(prompt, constraints);

        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.insets = new Insets(10,0,0,0);
        constraints.gridheight = 1;
        this.add(instructions, constraints);

        constraints.gridy = 3;
        constraints.insets = new Insets(0,0,0,0);
        this.add(roomNameField, constraints);

        constraints.gridy = 5;
        constraints.anchor = GridBagConstraints.CENTER;
        this.add(badNameWarning, constraints);

        constraints.ipady = 0;
        constraints.gridy = 6;
        constraints.gridx = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.anchor = GridBagConstraints.PAGE_END;
        constraints.insets = new Insets(5, 0, 20, 100);
        this.add(okButton, constraints);

        constraints.insets = new Insets(5, 100, 20, 0);
        this.add(backButton, constraints);

        this.setName(PanelNames.ROOM_NAME_PANEL);

        okButton.addActionListener(e -> {
            // validate input

            String roomName = roomNameField.getText();

            if (!ValidateInput.validateLength(roomName, Constants.MIN_USER_INPUT_LENGTH, Constants.MAX_USER_INPUT_LENGTH)
            || !ValidateInput.validateGeneric(roomName)) {
                triggerErrorMessage(badNameWarning);
                return;
            }

            RoomSetupWorker rsWorker = new RoomSetupWorker(roomName, userRef, chatUserLock, appState);
            rsWorker.start();

        });

        backButton.addActionListener(e -> {
            appState.setAppState(AppStateValue.CHOICE_PANEL);
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }
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
        badNameWarning.setText(WARNING_TEXT);
        Timer timer = new Timer(3500, event -> {
            badNameWarning.setText(" ");
        });
        timer.setRepeats(false);
        timer.start();
    }
}
