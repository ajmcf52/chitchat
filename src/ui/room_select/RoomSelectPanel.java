package ui.room_select;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import ui.room_select.RoomSelectTable;
import misc.PanelNames;

/**
 * this class represents the panel that shows all available chat rooms
 * that have been created and are open to join.
 */
public class RoomSelectPanel extends JPanel {
    
    private RoomSelectTable table; // displays all the room selection data
    private JButton backButton; // press to go to previous screen
    private JButton refreshButton; // press to refresh the list of rooms
    private JButton joinButton; // press to join a selected room
    private JScrollPane tablePane; // contains the room table
    private boolean isSelected; // flag indicating whether or not a room has been selected
    private JPanel refreshJoinPanel; // panel containing the refresh & join buttons

    /**
     * constructor for RSP
     */
    public RoomSelectPanel() {
        table = new RoomSelectTable();
        tablePane = new JScrollPane();
        backButton = new JButton("Back");
        refreshButton = new JButton("Refresh");
        joinButton = new JButton("Join");
        refreshJoinPanel = new JPanel();
        refreshJoinPanel.add(refreshButton);
        refreshJoinPanel.add(joinButton);
        refreshButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        joinButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        joinButton.setEnabled(false);
        isSelected = false;

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 5;
        constraints.gridheight = 4;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(40, 15, 60, 15);
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        this.add(tablePane, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LAST_LINE_START;
        constraints.insets = new Insets(0, 8, 8, 0);
        this.add(backButton, constraints);

        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.anchor = GridBagConstraints.LAST_LINE_END;
        this.add(refreshJoinPanel, constraints);

        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setRowSelectionAllowed(true);

        tablePane.setViewportView(table);
        table.setFillsViewportHeight(true);
        this.setName(PanelNames.ROOM_SELECT_PANEL_NAME);
        
        MyMouseListener listener = new MyMouseListener();
        this.addMouseListener(listener);
        table.addMouseListener(listener);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                joinButton.setEnabled(true);
            }
        });
        

    }

    public class MyMouseListener implements MouseListener {
        public MyMouseListener() {}

        @Override
        public void mouseClicked(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                table.clearSelection();
                joinButton.setEnabled(false);
            }
            
            
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub
            
        }


    }
}
