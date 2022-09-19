package ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dimension;

import misc.PanelNames;
import main.AppStateValue;
import main.ApplicationState;

/**
 * this class represents the panel after the alias selection screen, where users
 * choose between joining an existing room or creating a new one.
 */
public class ChoicePanel extends JPanel {

    private JButton joinRoomButton;
    private JButton createRoomButton;
    private final Font buttonFont = new Font("Serif", Font.BOLD, 24);

    // used to notify the ChatUser when vital setup steps have been completed.
    private Object chatUserLock;
    // application state (potentially modified by thread worker)
    private ApplicationState appState;

    /**
     * constructor for the ChoicePanel
     * 
     * @param ref      reference object to the ChatUser at hand
     * @param userLock lock used to notify said ChatUser when steps have been
     *                     completed.
     */
    public ChoicePanel(Object userLock, ApplicationState state) {
        chatUserLock = userLock;
        appState = state;

        joinRoomButton = new JButton();
        joinRoomButton.setText("Join Room");
        joinRoomButton.setFont(buttonFont);
        joinRoomButton.setVisible(true);
        joinRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        createRoomButton = new JButton();
        createRoomButton.setText("Create a Room");
        createRoomButton.setFont(buttonFont);
        createRoomButton.setVisible(true);
        createRoomButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // we use rigid areas (invisible by default) to create space between the
        // JButtons.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createRigidArea(new Dimension(0, 75)));
        this.add(joinRoomButton);
        this.add(Box.createRigidArea(new Dimension(0, 60)));
        this.add(createRoomButton);
        this.add(Box.createRigidArea(new Dimension(0, 75)));
        this.setName(PanelNames.CHOICE_PANEL);

        // action listeners here are pretty self-explanatory...

        createRoomButton.addActionListener(e -> {
            appState.setAppState(AppStateValue.ROOM_NAMING);
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }
        });

        joinRoomButton.addActionListener(e -> {
            appState.setAppState(AppStateValue.ROOM_SELECT);
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }
        });

    }
}
