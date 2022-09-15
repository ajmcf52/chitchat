package ui.room_select;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import main.ApplicationState;
import messages.ListRoomsMessage;
import main.AppStateValue;
import misc.PanelNames;
import misc.SharedValidateNotifier;
import misc.ValidateInput;
import net.ChatUser;
import worker.JoinRoomWorker;
import misc.Constants;

/**
 * this class represents the panel that shows all available chat rooms that have
 * been created and are open to join.
 */
public class RoomSelectPanel extends JPanel {

    private JButton backButton; // press to go to previous screen
    private JButton refreshButton; // press to refresh the list of rooms
    private JButton joinButton; // press to join a selected room
    private JScrollPane tablePane; // contains the room table
    private JPanel refreshJoinPanel; // panel containing the refresh & join buttons

    private static RoomSelectTable table; // displays all the room selection data
    private static Object workerNotifier; // this object is notified for "Refresh" requests
    private ApplicationState appState; // to be interacted with on particular button presses.

    private RoomsListFetcher roomsListFetcher; // thread-based worker used to fetch the list of rooms
    private ChatUser userRef; // reference to the chat user object
    private Object mainAppNotifier; // used to notify main() in ChatterApp.java

    private static String selectedRoomName; // for confirming that a room being joined still exists
    private static String selectedConnectInfo; // used to establish connection (JoinRoomWorker)
    private static Object preJoinNotifier; // for notifying that a pre-join refresh is complete

    private static SharedValidateNotifier svn; // used for room validation

    /**
     * constructor for RSP
     * 
     * @param state   the application's internal state
     * @param user    the chat user (i.e., Bob)
     * @param appLock AKA main app notifier
     */
    public RoomSelectPanel(ApplicationState state, ChatUser user, Object appLock) {
        this.setName(PanelNames.ROOM_SELECT_PANEL);
        appState = state;
        // fire up the RoomsListFetcher as quickly as possible to get our table
        // populated.
        table = new RoomSelectTable();
        workerNotifier = new Object();
        preJoinNotifier = new Object();
        svn = new SharedValidateNotifier();

        roomsListFetcher = new RoomsListFetcher(workerNotifier, preJoinNotifier, svn);
        userRef = user;
        mainAppNotifier = appLock;
        selectedRoomName = "";
        selectedConnectInfo = "";

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
        table.setIntercellSpacing(new Dimension(0, 0));
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

            /**
             * crucial method in making sure that selected values are set statically for all
             * RSP entities to observe.
             * 
             * @param e
             */
            public void valueChanged(ListSelectionEvent e) {
                int selectionIndexRow = e.getFirstIndex();
                selectedRoomName = (String) table.getValueAt(selectionIndexRow, Constants.ROOM_NAME_TABLE_COLUMN);
                selectedConnectInfo = (String) table.getValueAt(selectionIndexRow, Constants.IP_PORT_TABLE_COLUMN);
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
            int selectedRowNumber = table.getSelectedRow();
            String selectedRoomName = (String) table.getModel().getValueAt(selectedRowNumber,
                            Constants.ROOM_NAME_TABLE_COLUMN);
            new Thread() {
                public void run() {
                    attemptRoomJoin(selectedRoomName);
                }
            }.start();
        });

        backButton.addActionListener(e -> {
            /**
             * do this work in a spontaneous Thread to keep stress off the EDT.
             */
            new Thread() {
                public void run() {
                    roomsListFetcher.clearListingCache();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            DefaultTableModel model = (DefaultTableModel) table.getModel();
                            while (model.getRowCount() > 0) {
                                model.removeRow(model.getRowCount() - 1);
                            }
                        }
                    });
                    appState.setAppState(AppStateValue.CHOICE_PANEL);
                    synchronized (mainAppNotifier) {
                        mainAppNotifier.notify();
                    }
                }
            }.start();

        });
    }

    /**
     * clears all listing data local to this client, both within the model & the
     * RoomsListFetch worker.
     */
    public void clearLocalListings() {
        roomsListFetcher.clearListingCache();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                while (model.getRowCount() > 0) {
                    model.removeRow(model.getRowCount() - 1);
                }
            }
        });
    }

    /**
     * instantiates and starts the thread responsible for populating the rooms list
     * table. This persistent thread worker also handles refresh requests.
     */
    public void populateRoomsList() {
        if (!roomsListFetcher.isAlive()) {
            roomsListFetcher = new RoomsListFetcher(workerNotifier, preJoinNotifier, svn);
            roomsListFetcher.start();
        } else {
            // clearing the table model + cache for fresh population of lists.
            // SwingUtilities.invokeLater(new Runnable() {
            // public void run() {
            // DefaultTableModel model = (DefaultTableModel) table.getModel();
            // while (table.getModel().getRowCount() > 0) {
            // (model).removeRow(model.getRowCount() - 1);
            // }
            // }
            // });
            // roomsListFetcher.clearListingCache();
            synchronized (workerNotifier) {
                workerNotifier.notify();
            }
        }

    }

    /**
     * validation method for ensuring that the selected room indeed exists before we
     * attempt to join. Achieved with a quick pre-join refresh and simple room name
     * comparison.
     * 
     * @param selectedRoomName name of the room selected in the room list
     */
    public void attemptRoomJoin(String selectedRoomName) {
        String ipPortString = selectedConnectInfo;
        if (SwingUtilities.isEventDispatchThread()) {
            System.out.println("hihihi");
        }
        synchronized (svn) {
            svn.toggleRequest();
        }
        synchronized (workerNotifier) {
            workerNotifier.notify();
            try {
                workerNotifier.wait();
            } catch (Exception e) {
                System.out.println("RSP error waiting on RLF --> " + e.getMessage());
            }
        }

        // if we haven't found the room, alert the user and abort room joining.
        synchronized (svn) {
            if (!svn.readSuccessful()) {
                // TODO spawn a popup window with the room alert.
                String dialogMessage = "The room selected is no longer in existence.";
                JOptionPane.showMessageDialog(null, dialogMessage, "Room Disbanded", JOptionPane.WARNING_MESSAGE);
                svn.reset();
                return;
            }
            svn.reset();
        }
        JoinRoomWorker jrw = new JoinRoomWorker(ipPortString, userRef, selectedRoomName, mainAppNotifier, appState);
        jrw.start();
    }

    /**
     * this class is responsible for fetching rooms list data from the Registry,
     * which will then be used to populate the featured table with room listing
     * data.
     */
    private static class RoomsListFetcher extends Thread {
        private volatile boolean isRunning; // flag used to signal when work is complete
        private Object workerNotify; // notified on for critical tasks or exit signals
        private ArrayList<String> csvRoomDataObjs; // used for a point of reference when refreshing

        private Socket socket; // socket used for Registry communication
        private ObjectInputStream in; // input stream
        private ObjectOutputStream out; // output stream

        private SharedValidateNotifier svn; // shared validation notifier

        /**
         * constructor for RLF.
         * 
         * @param rn    the object by which we will wait for refresh requests
         * @param pjn   pre join notifier (notify on this post-refresh)
         * @param svn__ shared validation notifier
         */
        public RoomsListFetcher(Object rn, Object pjn, SharedValidateNotifier svn__) {
            isRunning = false;
            workerNotify = rn;
            preJoinNotifier = pjn;
            csvRoomDataObjs = new ArrayList<String>();
            socket = null;
            in = null;
            out = null;
            svn = svn__;
        }

        /**
         * this method is used to let the RLF worker that it can exit.
         */
        public void signalWorkComplete() {

            isRunning = false;
            this.interrupt();
            try {
                this.join();
            } catch (Exception e) {
                System.out.println("RSP error waiting for join() --> " + e.getMessage());
            }
        }

        /**
         * clears the local storage of room listing CSV objects.
         */
        public void clearListingCache() {
            csvRoomDataObjs.clear();
        }

        /**
         * this method pertains to servicing a refresh request for the list of rooms.
         * 
         * @param in  input reader
         * @param out output writer
         * @throws IOException
         * @throws NumberFormatException
         */
        public void serviceRefreshRequest() {

            Object obj = null;
            // NOTE this method is called from within a try/catch.
            try {
                socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                ListRoomsMessage requestMessage = new ListRoomsMessage();
                out.writeObject(requestMessage);
                out.flush();
                obj = in.readObject();

            } catch (Exception e) {
                System.out.println("RLF Error in communicating with Registry --> " + e.getMessage());
            }

            ListRoomsMessage response = ValidateInput.validateListRoomsMessage(obj);
            ArrayList<String> latestListings = response.getListings();

            if (latestListings.size() == 0) {
                // edge case: if true, just clear the model & list.
                DefaultTableModel model = (DefaultTableModel) table.getModel();

                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            while (table.getModel().getRowCount() > 0) {
                                ((DefaultTableModel) table.getModel()).removeRow(model.getRowCount() - 1);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            csvRoomDataObjs.clear();
            try {
                System.out.println("waiting for table updates to occur..");
                sleep(750);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * at this point, we have the current set of rooms; now we can compare with the
             * local CSV ArrayList and update the table's model accordingly.
             */
            int i = 0, j = 0, csvLen = csvRoomDataObjs.size(), latestLen = latestListings.size();

            while (i < csvLen && j < latestLen) {
                if (csvRoomDataObjs.get(i).equals(latestListings.get(j))) {
                    i++;
                    j++;
                } else {
                    /*
                     * A mismatch tells us that a room removal has occurred. We know this, because
                     * any additions would be added to the end.
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

            synchronized (svn) {

                if (svn.readRequested()) {
                    i = table.getSelectedRow();
                    while (i >= 0) {
                        String roomName = (String) table.getModel().getValueAt(i, Constants.ROOM_NAME_TABLE_COLUMN);
                        if (selectedRoomName.equals(roomName)) {
                            svn.markAsSuccessful();
                            break;
                        }
                        i--;
                    }
                    synchronized (workerNotifier) {
                        workerNotifier.notify();
                    }
                }
            }

        }

        /**
         * this thread's main line of execution.
         */
        public void run() {
            Object obj = null;

            isRunning = true;
            try {
                socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                // NOTE order of constructor calls is crucial here! Reference ChatUser.java for
                // more details.
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                ListRoomsMessage requestMessage = new ListRoomsMessage();
                out.writeObject(requestMessage);
                out.flush();

                // expected response is a ListRoomsMessage, equipped with a serialized list of
                // CSV-style room listings.
                // NOTE format of the CSV-style room listings is outlined closely in
                // ListRoomsMessage.java
                obj = in.readObject();
            } catch (Exception e) {
                System.out.println("RoomsListFetcher Error --> " + e.getMessage());
            }

            ListRoomsMessage response = ValidateInput.validateListRoomsMessage(obj);
            for (String csvListing : response.getListings()) {
                String[] listingArgs = csvListing.split(",");
                table.addEntry(listingArgs);
                csvRoomDataObjs.add(csvListing);
            }

            // principal list fetch complete; wait on user for additional RoomListing (i.e.,
            // Refresh) requests.
            while (true) {
                try {
                    synchronized (workerNotify) {
                        workerNotify.wait();
                    }
                } catch (Exception e) {
                    System.out.println("RLF Error waiting for work --> " + e.getMessage());
                }
                if (!isRunning) {
                    break; // work is done, time to break.
                }

                // otherwise, we can rightfully assume user is requesting a refresh.
                serviceRefreshRequest();
            }
            // work done; close streams and exit.
            try {
                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (Exception e) {
                System.out.println("RLF Error in closing socket --> " + e.getMessage());
            }
        }
    }

    /**
     * this class is used for the sake of deselecting room listing selections, and
     * disabling the "Join" button accordingly.
     */
    public class MyMouseListener implements MouseListener {
        public MyMouseListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        /**
         * this method is used to clear a given room listing selection when a user
         * clicks & releases either somewhere outside the table, or somewhere inside the
         * table that isn't on a listing.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                table.clearSelection();
                joinButton.setEnabled(false);
                selectedRoomName = "";
                selectedConnectInfo = "";
            }

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
