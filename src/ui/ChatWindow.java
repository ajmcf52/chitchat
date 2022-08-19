package ui;

import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.*;

import net.ChatUser;
import misc.TimeStampGenerator;
import misc.Constants;

/**
 * this class represents the popup chat window that is used for one ChatUser
 * to be able to communicate with other ChatUsers connected to the same session.
 */
public class ChatWindow extends JFrame {


    private static final int CHAT_CELL_HEIGHT = 15; // number of rows
    // private static final int PARTICIPANT_LIST_WIDTH = 20; // ibid, your honor!
    // private static final int PARTCIPANT_LIST_HEIGHT = 200; // number of rows
    private static final Font CHAT_PLACEHOLDER_FONT = new Font("Serif", Font.ITALIC, 14);
    private static final Font PARTICIPANT_LIST_LABEL_FONT = new Font("MONOSPACED", Font.BOLD | Font.ITALIC, 14);
    private static final int CHAT_TEXTBOX_WIDTH = 100; // width of chat textbox
    // private static final int CHAT_TEXTBOX_HEIGHT = 14;
    private static final String CHAT_PLACEHOLDER_STR = "Enter message here...";

    private String sessionID; // id unique to this session.
    private JPanel chatPanel; // panel of the chat window.

    // Variables with dynamic data below...
    private JList<String> chatFeed; // current state of the chat.
    private JTextField chatTextField; // where outgoing messages can be entered.
    private JList<String> participantList; // where participants are shown.
    private JLabel participantListLabel; // simple participant list label.
    private String chatFeedString; // text displayed in the chat feed. (messages separated by '\n')
    private String participantListString; //participant list string. (participants separated by '\n')

    /**
     * JList models for displaying lines of read-only text
     */
    private MyListModel participanListModel;
    private MyListModel chatFeedModel;

    private ChatUser chatUser; // user to which this chat window is dedicated.
    private UserOutputHandler outputHandler; // handles user-generated output events (i.e., sending a message)
    private final Object messageEventNotifier = new Object(); // notify this to trigger sending of a message
    /**
     * constructor for the chat window.
     * @param sid session id
     */
    public ChatWindow(String sid, ChatUser user) {
        chatUser = user;
        outputHandler = new UserOutputHandler(messageEventNotifier);
        outputHandler.start();

        chatFeedString = "";
        participantListString = "";
        sessionID = sid;
        this.setTitle("CHAT SESSION - " + sessionID);
        this.setSize(400, 400);
        this.setLocationRelativeTo(null);

        // instantiating objects
        participanListModel = new MyListModel();
        chatFeedModel = new MyListModel();
        chatPanel = new JPanel();
        chatFeed = new JList<String>(chatFeedModel);
        chatTextField = new JTextField("", CHAT_TEXTBOX_WIDTH);
        participantList = new JList<String>(participanListModel);
        participantListLabel = new JLabel("Participants");

        chatPanel.setLayout(new GridBagLayout());
        GridBagConstraints chatFeedConstraints = new GridBagConstraints();
        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        GridBagConstraints participantListConstraints = new GridBagConstraints();
        GridBagConstraints participantLabelConstraints = new GridBagConstraints();

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
        chatFeedConstraints.insets = new Insets(20, 10, 0, 0);

        textFieldConstraints.gridx = 0;
        textFieldConstraints.gridy = 17;
        textFieldConstraints.gridheight = 3;
        textFieldConstraints.gridwidth = 16;
        textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        textFieldConstraints.weightx = 0.8;
        textFieldConstraints.weighty = 0.15;
        textFieldConstraints.insets = new Insets(0, 5, 0, 5);

        participantListConstraints.gridx = 16;
        participantListConstraints.gridy = 2;
        participantListConstraints.gridheight = 18;
        participantListConstraints.gridwidth = 4;
        participantListConstraints.fill = GridBagConstraints.BOTH;
        participantListConstraints.weightx = 0.2;
        participantListConstraints.weighty = 0.0;
        participantListConstraints.insets = new Insets(20,10,5,5);
        participantListConstraints.anchor = GridBagConstraints.LAST_LINE_END;

        participantLabelConstraints.gridx = 16;
        participantLabelConstraints.gridy = 0;
        participantLabelConstraints.gridheight = 2;
        participantLabelConstraints.gridwidth = 4;
        participantLabelConstraints.insets = new Insets(5, 5,0,5);

        /**
         * miscellaneous editing of object properties...
         */

        chatFeed.setFixedCellHeight(CHAT_CELL_HEIGHT);
        Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder,"Chat Feed");
        titledBorder.setTitleJustification(TitledBorder.ABOVE_TOP);
        chatFeed.setBorder(titledBorder);
        
        // add the initial messages (if there are any)
        MyListModel model = (MyListModel) chatFeed.getModel();
        String[] messages = chatFeedString.split("\n");
        for (int i = 0; i < messages.length; i++) {
            model.addElement(messages[i]);
        }

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
        
        model = (MyListModel) participantList.getModel();
        messages = participantListString.split("\n");
        for (int i = 0; i < messages.length; i++) {
            model.addElement(messages[i]);
        }

        /**
         * This focus listener ensures we have 
         * nicely formatted placeholder text :)
         */
        chatTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (chatTextField.getText().equals(CHAT_PLACEHOLDER_STR)) {
                    chatTextField.setText("");
                chatTextField.setForeground(Color.BLACK);
                }

            }
            @Override
            public void focusLost(FocusEvent e) {
                if (chatTextField.getText().isEmpty()) {
                    chatTextField.setText(CHAT_PLACEHOLDER_STR);
                    chatTextField.setForeground(Color.LIGHT_GRAY);
                }
            }
        });

        chatTextField.addKeyListener(new KeyListener() {

            // extraneous methods
            public void keyTyped(KeyEvent e) { }
            public void keyPressed(KeyEvent e) { }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != Constants.KC_RETURN) {
                    // only interested in events generated by the return key (i.e., "Enter")
                    return;
                }
                // all we have to do is notify; UOW handles the rest.
                messageEventNotifier.notify();
            }
            
        });
        
        chatPanel.add(chatFeed, chatFeedConstraints);
        chatPanel.add(chatTextField, textFieldConstraints);
        chatPanel.add(participantList, participantListConstraints);
        chatPanel.add(participantListLabel, participantLabelConstraints);

        this.setContentPane(chatPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * method used to add a line of text to the chat feed.
     * @param line text to be added.
     */
    public void addLineToFeed(String line) {
        chatFeedModel.addElement(line);
    }

    /**
     * used to add a name to the list of participants in the chat.
     * @param name alias of the user to be added.
     */
    public void addParticipantName(String name) {
        participanListModel.addElement(name);
    }

    /**
     * used to remove a name from the list of participants in the chat.
     * @param name alias of the user to be removed.
     */
    public void removeParticipantName(String name) {
        participanListModel.removeElement(name);
    }

    /**
     * this class is responsible for retrieving passing along user-supplied
     * information (i.e., text messages, exit events, etc) to the user's OutputWorker.
     */
    public class UserOutputHandler extends Thread {

        private Object eventNotifier; // UOH waits on this for various events to pop up for it to handle
        private volatile boolean isRunning; // flag used to signal a shut down
        private final Object runLock = new Object(); // lock object used to externally signal a shut down while avoiding race conditions

        /**
         * constructor for UOH.
         * @param user reference to the user attached to the chat window.
         * @param notifier object used to notify this worker of events needing to be handled.
         */
        public UserOutputHandler(Object notifier) {
            eventNotifier = notifier;
            isRunning = false;
        }

        /**
         * this worker's main line of execution.
         */
        public void run() {
            isRunning = true;

            System.out.println("UserOutputHandler has booted; waiting for events...");
            while (isRunning) {
                try {
                    synchronized (eventNotifier) {
                        eventNotifier.wait();
                    }
                } catch (Exception e) {
                    System.out.println("UOH Error! --> " + e.getMessage());
                }
                // UOH has been woken up; check for an event to handle
                String toSend = chatTextField.getText();
                if (!toSend.isEmpty()) {
                    // clear the text
                    chatTextField.setText("");
                    // package the message into an acceptable format, and push it along to the ChatUser's OutputWorker.
                    String timestamp = TimeStampGenerator.now();
                    String completeMsg = timestamp + Constants.DELIM + toSend + '\n';
                    chatUser.pushOutgoingMessage(completeMsg);
                }

                synchronized (runLock) {
                    if (!isRunning) {
                        break;
                    }
                }
            }
        }

        /**
         * method used to externally signal a shut down to this worker.
         */
        public void turnOff() {
            synchronized (runLock) {
                isRunning = false;
            }
        }
    }
}
