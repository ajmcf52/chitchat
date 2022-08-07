package ui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dimension;

import misc.PanelNames;
import net.ChatUser;
import worker.RoomSetupWorker;
import main.ApplicationState;

/**
 * this class represents the panel after the alias selection screen,
 * 
 */
public class ChoicePanel extends JPanel {

    private JButton joinRoomButton;
    private JButton createRoomButton;
    private final Font buttonFont = new Font("Serif", Font.BOLD, 24);

    // used to send reference user info being sent to the registry.
    private ChatUser userRef;
    // used to notify the ChatUser when vital setup steps have been completed.
    private Object chatUserLock;
    // application state (potentially modified by thread worker)
    private ApplicationState appState;

    /**
     * constructor for the ChoicePanel
     * @param ref reference object to the ChatUser at hand
     * @param userLock lock used to notify said ChatUser when steps have been completed.
     */
    public ChoicePanel(ChatUser ref, Object userLock, ApplicationState state) {
        userRef = ref;
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
  
        // we use rigid areas (invisible by default) to create space between the JButtons.
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createRigidArea(new Dimension(0, 75)));
        this.add(joinRoomButton);
        this.add(Box.createRigidArea(new Dimension(0, 60)));
        this.add(createRoomButton);
        this.add(Box.createRigidArea(new Dimension(0, 75)));
        this.setName(PanelNames.CHOICE_PANEL_NAME);

        createRoomButton.addActionListener(e -> {
            // connect to the registry and send a NewRoomRequest
            // ^^this is done by the RoomSetupWorker....
            System.out.println("create room clicked");
            RoomSetupWorker worker = new RoomSetupWorker(userRef, chatUserLock, appState);
            worker.start();

        });

    }
}
