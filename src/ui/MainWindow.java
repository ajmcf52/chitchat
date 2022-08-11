package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

import misc.PanelNames;

import java.awt.CardLayout;

public class MainWindow extends JFrame {
    
    private JPanel cardStack;
    private CardLayout layout;

    public MainWindow(JPanel... panels) {
        this.setTitle("Chatter");
        this.setSize(500,300);
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
        layout.show(cardStack, PanelNames.LOGIN_PANEL_NAME);
    }

    /**
     * this method is called to flip to the 2nd card in
     * the content pane's CardLayout, ChoicePanel.
     */
    public void showChoicePanel() {
        layout.show(cardStack, PanelNames.CHOICE_PANEL_NAME);
    }

    /**
     * called to show the room selection panel.
     */
    public void showRoomSelectPanel() {
        layout.show(cardStack, PanelNames.ROOM_SELECT_PANEL_NAME);
        this.pack();
    }

    /*
     * HelloWorldDisplay displayPanel = new HelloWorldDisplay();
      JButton okButton = new JButton("OK");
      ButtonHandler listener = new ButtonHandler();
      okButton.addActionListener(listener);

      JPanel content = new JPanel();
      content.setLayout(new BorderLayout());
      content.add(displayPanel, BorderLayout.CENTER);
      content.add(okButton, BorderLayout.SOUTH);

      JFrame window = new JFrame("GUI Test");
      window.setContentPane(content);
      window.setSize(250,100);
      window.setLocation(100,100);
      window.setVisible(true);
     */
}
