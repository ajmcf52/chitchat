package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * this class represents the popup chat window that is used for one ChatUser
 * to be able to communicate with other ChatUsers connected to the same session.
 */
public class ChatWindow extends JFrame {
    
    private String sessionID; // id unique to this session.
    private JPanel chatPanel; // panel of the chat window.

    /**
     * constructor for the chat window.
     * @param sid session id
     */
    public ChatWindow(String sid) {
        sessionID = sid;
        this.setTitle("CHAT SESSION - " + sessionID);
        this.setSize(400, 400);
        this.setLocationRelativeTo(null);

        chatPanel = new JPanel();
        
    }
}
