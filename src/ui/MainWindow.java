package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

import misc.PanelNames;
import ui.room_select.RoomSelectPanel;

import java.awt.CardLayout;

public class MainWindow extends JFrame {
    
    private JPanel cardStack;
    private CardLayout layout;

    public MainWindow(JPanel... panels) {
        this.setTitle("Chatter");
        this.setSize(600,400);
        this.setLocationRelativeTo(null); // centers the component
        
        cardStack = new JPanel();
        layout = new CardLayout();
        cardStack.setLayout(layout);

        int numPanels = panels.length;
        for (int i = 0; i < numPanels; i++) {
            cardStack.add(panels[i], panels[i].getName());
        }

        this.setContentPane(cardStack);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * called to show the login panel.
     * Likely won't be of use until if and when we implement name changing
     */
    public void showLoginPanel() {
        layout.show(cardStack, PanelNames.LOGIN_PANEL);
    }

    /**
     * this method is called to flip to the 2nd card in
     * the content pane's CardLayout, ChoicePanel.
     */
    public void showChoicePanel() {
        layout.show(cardStack, PanelNames.CHOICE_PANEL);
    }

    /**
     * called to show the room selection panel. Also populates the table.
     * @rsp the panel whose table is being populated
     */
    public void showRoomSelectPanel(RoomSelectPanel rsp) {
        layout.show(cardStack, PanelNames.ROOM_SELECT_PANEL);
        populateRoomSelectTable(rsp);
        this.pack();
    }

    /**
     * initializes population of RoomSelectPanel's table of listed rooms.
     * @param rsp the panel.
     */
    public void populateRoomSelectTable(RoomSelectPanel rsp) {
        rsp.populateRoomsList();
    }

    /**
     * called to show the room naming panel.
     */
    public void showRoomNamingPanel() {
        layout.show(cardStack, PanelNames.ROOM_NAME_PANEL);
    }
}
