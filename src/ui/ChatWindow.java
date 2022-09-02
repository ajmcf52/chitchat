package ui;

import io.user.UserOutputHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.*;
import misc.Constants;
import misc.TimeStampGenerator;
import net.ChatUser;
import requests.ExitRoomWorker;

/**
 * this class represents the popup chat window that is used for one ChatUser to
 * be able to communicate with other ChatUsers connected to the same session.
 */
public class ChatWindow extends JFrame {

    private static final int CHAT_CELL_HEIGHT = 15; // number of rows
    // private static final int PARTICIPANT_LIST_WIDTH = 20; // ibid, your honor!
    // private static final int PARTCIPANT_LIST_HEIGHT = 200; // number of rows
    private static final Font CHAT_PLACEHOLDER_FONT = new Font("Serif", Font.ITALIC, 14);
    private static final Font CHAT_TYPING_FONT = new Font("Serif", Font.PLAIN, 14);
    private static final Font PARTICIPANT_LIST_LABEL_FONT = new Font("MONOSPACED", Font.BOLD | Font.ITALIC, 14);
    private static final int CHAT_TEXTBOX_WIDTH = 100; // width of chat textbox
    // private static final int CHAT_TEXTBOX_HEIGHT = 14;
    private static final String CHAT_PLACEHOLDER_STR = "Enter message here...";

    private String sessionID; // id unique to this session.
    private JPanel chatPanel; // panel of the chat window.

    // Variables with dynamic data below...
    private JList<String> chatFeed; // current state of the chat.
    private JTextField chatTextField; // where outgoing messages can be entered.
    private JButton sendMsgButton; // button used to send a message.
    private JButton exitButton; // button used to exit the chat.
    private JList<String> participantList; // where participants are shown.
    private JLabel participantListLabel; // simple participant list label.
    // private String chatFeedString; // text displayed in the chat feed. (messages
    // separated by '\n')
    // private String participantListString; //participant list string.
    // (participants separated by '\n')

    /**
     * JList models for displaying lines of read-only text
     */
    // private MyListModel participanListModel;
    // private MyListModel chatFeedModel;
    private DefaultListModel<String> participantListModel;
    private DefaultListModel<String> chatFeedModel;

    private ChatUser chatUser; // user to which this chat window is dedicated.
    private UserOutputHandler outputHandler; // handles user-generated output events (i.e., sending a message)
    private final Object messageEventNotifier = new Object(); // notify this to trigger sending of a message

    /**
     * constructor for the chat window.
     * 
     * @param sid session id
     */
    public ChatWindow(String sid, ChatUser user) {
        chatUser = user;
        outputHandler = new UserOutputHandler(messageEventNotifier, chatUser, this);
        outputHandler.start();

        // chatFeedString = "";
        // participantListString = "";
        sessionID = sid;
        this.setTitle("CHAT SESSION - " + sessionID);
        this.setSize(400, 400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // NOTE important to not exit on close.

        // instantiating objects
        participantListModel = new MyListModel();
        chatFeedModel = new MyListModel();
        chatPanel = new JPanel();
        chatFeed = new JList<String>(chatFeedModel);
        chatTextField = new JTextField("", CHAT_TEXTBOX_WIDTH);
        sendMsgButton = new JButton("Send");
        exitButton = new JButton("Exit");
        participantList = new JList<String>(participantListModel);
        participantListLabel = new JLabel("Participants");

        chatPanel.setLayout(new GridBagLayout());
        GridBagConstraints chatFeedConstraints = new GridBagConstraints();
        GridBagConstraints sendMsgButtonConstraints = new GridBagConstraints();
        GridBagConstraints exitButtonConstraints = new GridBagConstraints();
        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        GridBagConstraints participantListConstraints = new GridBagConstraints();
        GridBagConstraints participantLabelConstraints = new GridBagConstraints();

        /**
         * shield your eyes from the magic constants!!! Ahhhhhh!!!!!
         *
         * Kidding... I'll do my best to explain things here. Operating on the basis of
         * a 20x20 grid spread in a default frame size of 400x400 pixels.
         *
         * these constraint object parameters essentially dictate the sizing and
         * positioning of our objects within our GridBagLayout. The meaning behind all
         * of these constraint properties can be found at this link here:
         * https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
         *
         * One thing to note is the weight chosen.. I went with 0.5 across the border,
         * as 0.0 and 1.0 are considered to be the extremes. Considering I am fairly new
         * to GridBagLayout, I went with the most moderate option possible.
         *
         * All the other fields are pretty self-explanatory, but if you seek further
         * clarification, feel free to check out the aforementioned link! :)
         */
        chatFeedConstraints.gridx = 0;
        chatFeedConstraints.gridy = 0;
        chatFeedConstraints.gridheight = 18;
        chatFeedConstraints.gridwidth = 16;
        chatFeedConstraints.fill = GridBagConstraints.BOTH;
        chatFeedConstraints.weightx = 0.8;
        chatFeedConstraints.weighty = 0.85;
        chatFeedConstraints.insets = new Insets(5, 10, 0, 0);

        textFieldConstraints.gridx = 0;
        textFieldConstraints.gridy = 18;
        textFieldConstraints.gridheight = 1;
        textFieldConstraints.gridwidth = 16;
        textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        textFieldConstraints.weightx = 0.8;
        textFieldConstraints.weighty = 0.15;
        textFieldConstraints.insets = new Insets(0, 5, 0, 5);

        sendMsgButtonConstraints.gridx = 15;
        sendMsgButtonConstraints.gridy = 19;
        sendMsgButtonConstraints.gridheight = 1;
        sendMsgButtonConstraints.gridwidth = 1;
        sendMsgButtonConstraints.fill = GridBagConstraints.NONE;
        sendMsgButtonConstraints.weightx = 0.1;
        sendMsgButtonConstraints.weighty = 0.1;
        sendMsgButtonConstraints.insets = new Insets(0, 170, 0, 0);

        exitButtonConstraints.gridx = 1;
        exitButtonConstraints.gridy = 19;
        exitButtonConstraints.gridheight = 1;
        exitButtonConstraints.gridwidth = 1;
        exitButtonConstraints.fill = GridBagConstraints.NONE;
        exitButtonConstraints.weightx = 0.1;
        exitButtonConstraints.weighty = 0.1;
        exitButtonConstraints.insets = new Insets(0, 0, 0, 0);

        participantListConstraints.gridx = 16;
        participantListConstraints.gridy = 2;
        participantListConstraints.gridheight = 18;
        participantListConstraints.gridwidth = 4;
        participantListConstraints.fill = GridBagConstraints.BOTH;
        participantListConstraints.weightx = 0.2;
        participantListConstraints.weighty = 0.0;
        participantListConstraints.insets = new Insets(20, 10, 5, 5);
        participantListConstraints.anchor = GridBagConstraints.LAST_LINE_END;

        participantLabelConstraints.gridx = 16;
        participantLabelConstraints.gridy = 0;
        participantLabelConstraints.gridheight = 2;
        participantLabelConstraints.gridwidth = 4;
        participantLabelConstraints.insets = new Insets(5, 5, 0, 5);

        /**
         * miscellaneous editing of object properties...
         */

        chatFeed.setFixedCellHeight(CHAT_CELL_HEIGHT);
        Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "Chat Feed");
        titledBorder.setTitleJustification(TitledBorder.ABOVE_TOP);
        chatFeed.setBorder(titledBorder);
        chatFeed.setVisibleRowCount(-1);
        chatFeed.setSelectionBackground(Color.WHITE);
        chatFeed.setSelectionForeground(Color.BLACK);

        chatTextField.setText(CHAT_PLACEHOLDER_STR);
        chatTextField.setForeground(Color.LIGHT_GRAY);
        chatTextField.setFont(CHAT_PLACEHOLDER_FONT);
        chatTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        participantListLabel.setForeground(Color.DARK_GRAY);
        participantListLabel.setFont(PARTICIPANT_LIST_LABEL_FONT);

        participantList.setFixedCellHeight(CHAT_CELL_HEIGHT);
        Border raisedBevel, loweredBevel, compound;
        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        compound = BorderFactory.createCompoundBorder(raisedBevel, loweredBevel);
        participantList.setBorder(compound);
        participantList.setVisibleRowCount(-1);
        participantList.setSelectionBackground(Color.WHITE);
        participantList.setSelectionForeground(Color.BLACK);

        /**
         * This focus listener ensures we have nicely formatted placeholder text.
         */
        chatTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (chatTextField.getText().equals(CHAT_PLACEHOLDER_STR)) {
                    chatTextField.setText("");
                    chatTextField.setForeground(Color.BLACK);
                    chatTextField.setFont(CHAT_TYPING_FONT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (chatTextField.getText().isEmpty()) {
                    chatTextField.setFont(CHAT_PLACEHOLDER_FONT);
                    chatTextField.setText(CHAT_PLACEHOLDER_STR);
                    chatTextField.setForeground(Color.LIGHT_GRAY);
                }
            }
        });

        chatTextField.addKeyListener(new KeyListener() {
            // extraneous methods
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != Constants.KC_RETURN) {
                    // only interested in events generated by the return key (i.e., "Enter")
                    return;
                }
                // all we have to do is notify; UOW handles the rest.
                sendMsgButton.doClick();
            }
        });

        exitButton.addActionListener(e -> {
            ExitRoomWorker erw = new ExitRoomWorker(chatUser);
            erw.start();
        });

        sendMsgButton.addActionListener(e -> {
            String msgText = chatTextField.getText();
            if (msgText.isEmpty()) {
                return;
            }
            String timestamp = TimeStampGenerator.now();
            String selfMsg = "[" + timestamp + "]" + " You: " + msgText;
            addLineToFeed(selfMsg);
            // chatFeed.ensureIndexIsVisible(chatFeedModel.size());
            // chatFeed.requestFocus();

            synchronized (messageEventNotifier) {
                messageEventNotifier.notify();
            }
        });

        chatPanel.add(chatFeed, chatFeedConstraints);
        chatPanel.add(chatTextField, textFieldConstraints);
        chatPanel.add(sendMsgButton, sendMsgButtonConstraints);
        chatPanel.add(exitButton, exitButtonConstraints);
        chatPanel.add(participantList, participantListConstraints);
        chatPanel.add(participantListLabel, participantLabelConstraints);

        this.setContentPane(chatPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * method used to add a line of text to the chat feed.
     * 
     * @param line text to be added.
     */
    public void addLineToFeed(String line) {
        chatFeedModel.addElement(line);
        chatFeed.ensureIndexIsVisible(chatFeedModel.size());
        chatFeed.requestFocus();
    }

    /**
     * used to add a name to the list of participants in the chat.
     * 
     * @param name alias of the user to be added.
     */
    public void addParticipantName(String name) {
        participantListModel.addElement(name);
        participantList.ensureIndexIsVisible(participantListModel.size());
        participantList.requestFocus();
    }

    /**
     * used to remove a name from the list of participants in the chat.
     * 
     * @param name alias of the user to be removed.
     */
    public void removeParticipantName(String name) {
        participantListModel.removeElement(name);
    }

    /**
     * helper method used to retrieve and reset the text within the chat window's
     * text field.
     * 
     * @return String-based message to be sent to others in the chat room.
     */
    public String retrieveChatFieldText() {
        String chatText = chatTextField.getText();
        chatTextField.setText("");
        return chatText;
    }

    /**
     * Getter for the associated ChatUser's alias. Used to confirming which messages
     * should be received or discarded.
     * 
     * @return the associated ChatUser's alias
     */
    public String getAssociatedAlias() {
        return chatUser.getAlias();
    }
}
