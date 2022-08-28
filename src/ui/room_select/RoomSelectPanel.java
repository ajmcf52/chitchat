package ui.room_select;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import main.ApplicationState;
import messages.ListRoomsMessage;
import main.AppStateValue;
import misc.PanelNames;
import misc.Requests;
import misc.ValidateInput;
import net.ChatUser;
import misc.Constants;

import requests.JoinRoomWorker;


/**
 * this class represents the panel that shows all available chat rooms
 * that have been created and are open to join.
 */
public class RoomSelectPanel extends JPanel {
    
    private JButton backButton; // press to go to previous screen
    private JButton refreshButton; // press to refresh the list of rooms
    private JButton joinButton; // press to join a selected room
    private JScrollPane tablePane; // contains the room table
    private JPanel refreshJoinPanel; // panel containing the refresh & join buttons
    
    private static RoomSelectTable table; // displays all the room selection data
    private Object workerNotifier; // this object is notified for "Refresh" requests
    private ApplicationState appState; // to be interacted with on particular button presses.

    private RoomsListFetcher roomsListFetcher; // thread-based worker used to fetch the list of rooms
    private ChatUser userRef; // reference to the chat user object
    private Object chatUserLock; // lock object for communicating synchronous events with the user

    /**
     * constructor for RSP
     * @param state the application's internal state
     * @param user the chat user (i.e., Bob)
     * @param userLock lock used to communicate crucial events with the user
     */
    public RoomSelectPanel(ApplicationState state, ChatUser user, Object userLock) {
        this.setName(PanelNames.ROOM_SELECT_PANEL);
        appState = state;
        // fire up the RoomsListFetcher as quickly as possible to get our table populated.
        table = new RoomSelectTable();
        workerNotifier = new Object();

        roomsListFetcher = new RoomsListFetcher(workerNotifier);
        userRef = user;
        chatUserLock = userLock;
        
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
        this.setName(PanelNames.ROOM_SELECT_PANEL);
        
        MyMouseListener listener = new MyMouseListener();
        this.addMouseListener(listener);
        table.addMouseListener(listener);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                joinButton.setEnabled(true);
            }
        });

        refreshButton.addActionListener(e -> {
            refreshButton.setEnabled(false);
            synchronized (workerNotifier) {
                workerNotifier.notify();
            }
            
            Timer timer = new Timer(3000, event -> {
                // re-enable "Refresh" capability after 7.5 seconds
                refreshButton.setEnabled(true);
            });
            timer.setRepeats(false);
            timer.start();
        });

        joinButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) // "Join" when getSelectedRow() == -1, so this should really never happen.
                return;
            String ipPortString = (String) table.getModel().getValueAt(row, Constants.IP_PORT_TABLE_COLUMN);
            String roomName = (String) table.getModel().getValueAt(row, Constants.ROOM_NAME_TABLE_COLUMN);
            JoinRoomWorker jrw = new JoinRoomWorker(ipPortString, userRef, roomName, chatUserLock, state);
            jrw.start();
        });

        backButton.addActionListener(e -> {
            roomsListFetcher.signalWorkComplete();
            synchronized (workerNotifier) {
                workerNotifier.notify();
            }
            appState.setAppState(AppStateValue.CHOICE_PANEL);
        });
    }

    /**
     * instantiates and starts the thread responsible for populating the rooms list table.
     * This persistent thread worker also handles refresh requests.
     */
    public void populateRoomsList() {
        roomsListFetcher.start();
    }

    /**
     * this class is responsible for fetching rooms list data from
     * the Registry, which will then be used to populate the 
     * featured table with room listing data.
     */
    private static class RoomsListFetcher extends Thread {
        private volatile boolean isRunning; // flag used to signal when work is complete
        private Object workerNotify; // notified on for critical tasks or exit signals
        private ArrayList<String> csvRoomDataObjs; // used for a point of reference when refreshing
        /**
         * constructor for RLF.
         * @param rn the object by which we will wait for refresh requests
         */
        public RoomsListFetcher(Object rn) {
            isRunning = false;
            workerNotify = rn;
            csvRoomDataObjs = new ArrayList<String>();
        }

        /**
         * this method is used to let the RLF worker that it can exit.
         */
        public void signalWorkComplete() {
            isRunning = false;
            this.interrupt();
        }

        /**
         * this method pertains to servicing a refresh request for the list of rooms.
         * @param in input reader
         * @param out output writer
         * @throws IOException
         * @throws NumberFormatException
         */
        public void serviceRefreshRequest(ObjectInputStream in, ObjectOutputStream out) throws IOException, NumberFormatException, ClassNotFoundException {
            
            // NOTE this method is called from within a try/catch.

            ListRoomsMessage requestMessage = new ListRoomsMessage();
            out.writeObject(requestMessage);
            out.flush();

            Object obj = in.readObject();
            ListRoomsMessage response = ValidateInput.validateListRoomsMessage(obj);
            ArrayList<String> latestListings = response.getListings();
            /**
             * at this point, we have the current set of rooms; now we can 
             * compare with the local CSV ArrayList and update the table's model accordingly.
             */
            int i = 0, j = 0, csvLen = csvRoomDataObjs.size(), latestLen = latestListings.size();

            while (i < csvLen && j < latestLen) {
                if (csvRoomDataObjs.get(i).equals(latestListings.get(j))) {
                    i++;
                    j++;
                }
                else {
                    /*
                     * A mismatch tells us that a room removal has occurred.
                     * We know this, because any additions would be added to the end.
                     */

                    table.removeEntry(i);
                    csvRoomDataObjs.remove(i);
                }
            }
            // add any newly added rooms caught by the refresh.
            while (j < latestLen) {
                String[] newEntry = latestListings.get(j).split(",");
                table.addEntry(newEntry);
                csvRoomDataObjs.add(latestListings.get(j));
                j++;
            }
            // room listings refresh complete.
        }

        /**
         * this thread's main line of execution.
         */
        public void run() {
            Socket socket;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;

            isRunning = true;
            try {
                socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                // NOTE order of constructor calls is crucial here! Reference ChatUser.java for more details.
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                ListRoomsMessage requestMessage = new ListRoomsMessage();
                out.writeObject(requestMessage);
                out.flush();

                // expected response is a ListRoomsMessage, equipped with a serialized list of CSV-style room listings.
                // NOTE format of the CSV-style room listings is outlined closely in ListRoomsMessage.java 
                Object obj = in.readObject();
                ListRoomsMessage response = ValidateInput.validateListRoomsMessage(obj);
                for (String csvListing : response.getListings()) {
                    String[] listingArgs = csvListing.split(",");
                    table.addEntry(listingArgs);
                    csvRoomDataObjs.add(csvListing);
                }

                // principal list fetch complete; wait on user for additional RoomListing (i.e., Refresh) requests.
                while (true) {
                    synchronized (workerNotify) {
                        workerNotify.wait();
                    }
                    if (!isRunning) {
                        break; // work is done, time to break.
                    }
                    // otherwise, we can rightfully assume user is requesting a refresh.
                    serviceRefreshRequest(in, out);
                }
                // work done; close streams and exit.
                socket.close(); // NOTE this closes both associated streams as well.

            }
            catch (Exception e) {
                System.out.println("RoomsListFetcher Error --> " + e.getMessage());
            }
        }
    }

    /**
     * this class is used for the sake of deselecting room listing selections,
     * and disabling the "Join" button accordingly.
     */
    public class MyMouseListener implements MouseListener {
        public MyMouseListener() {}

        @Override
        public void mouseClicked(MouseEvent e) { }

        @Override
        public void mousePressed(MouseEvent e) { }

        /**
         * this method is used to clear a given room listing selection
         * when a user clicks & releases either somewhere outside the table,
         * or somewhere inside the table that isn't on a listing.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                table.clearSelection();
                joinButton.setEnabled(false);
            }
            
            
        }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }
    }
}
