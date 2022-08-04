package ui;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * this class represents the popup chat window that is used for one ChatUser
 * to be able to communicate with other ChatUsers connected to the same session.
 */
public class ChatWindow extends JFrame {


    private static final int CHAT_FEED_HEIGHT = 90; // number of rows
    private static final int CHAT_FEED_WIDTH = 80; // number of columns
    private static final int PARTICIPANT_LIST_WIDTH = 20; // ibid, your honor!
    private static final int PARTCIPANT_LIST_HEIGHT = 200; // number of rows

    private String sessionID; // id unique to this session.
    private JPanel chatPanel; // panel of the chat window.
    // private JPanel leftSidePanel; // contains chat feed and message control.
    // private JPanel rightSidePanel; // contains solely the participant list.

    // Variables with dynamic data below...
    private JTextArea chatFeed; // current state of the chat.
    private JTextField chatTextField; // where outgoing messages can be entered.
    private JTextArea participantList; // where participants are shown.



    /**
     * constructor for the chat window.
     * @param sid session id
     */
    public ChatWindow(String sid) {
        sessionID = sid;
        this.setTitle("CHAT SESSION - " + sessionID);
        this.setSize(400, 400);
        this.setLocationRelativeTo(null);

        // instantiating objects
        chatPanel = new JPanel();
        // leftSidePanel = new JPanel();
        // rightSidePanel = new JPanel();
        chatFeed = new JTextArea(CHAT_FEED_HEIGHT, CHAT_FEED_WIDTH);
        chatTextField = new JTextField("", CHAT_FEED_WIDTH);
        participantList = new JTextArea(PARTCIPANT_LIST_HEIGHT, PARTICIPANT_LIST_WIDTH);

        chatPanel.setLayout(new GridBagLayout());
        GridBagConstraints chatFeedConstraints = new GridBagConstraints();
        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        GridBagConstraints participantListConstraints = new GridBagConstraints();

        /**
         * shield your eyes from the magic constants!!! Ahhhhhh!!!!!
         * 
         * Kidding... I'll do my best to explain things here.
         * Operating on the basis of a 20x20 grid spread in a default frame size of 400x400 pixels.
         * 
         * these constraint object parameters essentially dictate the sizing and positioning of
         * our objects within our GridBagLayout. The meaning behind all of these constraint properties
         * can be found at this link here: https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
         * 
         * One thing to note is the weight chosen.. I went with 0.5 across the border, as 0.0 and 1.0 are
         * considered to be the extremes. Considering I am fairly new to GridBagLayout, I went with the most
         * moderate option possible.
         * 
         * All the other fields are pretty self-explanatory, but if you seek further clarification, feel free
         * to check out the aforementioned link! :)
         */
        chatFeedConstraints.gridx = 0;
        chatFeedConstraints.gridy = 0;
        chatFeedConstraints.gridheight = 17;
        chatFeedConstraints.gridwidth = 16;
        chatFeedConstraints.fill = GridBagConstraints.BOTH;
        chatFeedConstraints.weightx = 0.8;
        chatFeedConstraints.weighty = 0.85;

        textFieldConstraints.gridx = 17;
        textFieldConstraints.gridy = 0;
        textFieldConstraints.gridheight = 3;
        textFieldConstraints.gridwidth = 16;
        textFieldConstraints.fill = GridBagConstraints.BOTH;
        textFieldConstraints.weightx = 0.8;
        textFieldConstraints.weighty = 0.15;

        participantListConstraints.gridx = 0;
        participantListConstraints.gridy = 16;
        participantListConstraints.gridheight = 20;
        participantListConstraints.gridwidth = 4;
        participantListConstraints.fill = GridBagConstraints.BOTH;
        participantListConstraints.weightx = 0.2;
        participantListConstraints.weighty = 0.0;

        chatPanel.add(chatFeed, chatFeedConstraints);
        chatPanel.add(chatTextField, textFieldConstraints);
        chatPanel.add(participantList, participantListConstraints);

        this.setContentPane(chatPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
