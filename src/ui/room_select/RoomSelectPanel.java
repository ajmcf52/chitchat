package ui.room_select;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import ui.room_select.RoomSelectTable;

/**
 * this class represents the panel that shows all available chat rooms
 * that have been created and are open to join.
 */
public class RoomSelectPanel extends JPanel {
    
    private RoomSelectTable table; // displays all the room selection data
    private JButton backButton; // press to go to previous screen
    private JButton joinButton; // press to join a selected room
    private boolean isSelected; // flag indicating whether or not a room has been selected

    /**
     * constructor for RSP
     */
    public RoomSelectPanel() {
        table = new RoomSelectTable();
        backButton = new JButton("Back");
        joinButton = new JButton("Join");
        joinButton.setEnabled(false);
        isSelected = false;

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(40, 15, 60, 15);
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        this.add(table, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(0, 12, 12, 0);
        this.add(backButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        this.add(joinButton, constraints);

    }
}
