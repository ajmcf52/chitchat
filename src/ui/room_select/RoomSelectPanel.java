package ui.room_select;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import misc.PanelNames;
import misc.Constants;

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
    private Object refreshNotifier; // this object is notified for "Refresh" requests

    /**
     * constructor for RSP
     */
    public RoomSelectPanel() {
        // fire up the RoomsListFetcher as quickly as possible to get our table populated.
        table = new RoomSelectTable();
        refreshNotifier = new Object();
        
        RoomsListFetcher rlh = new RoomsListFetcher(refreshNotifier);
        rlh.start();
        
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

        public void signalWorkComplete() {
            isRunning = false;
        }

        /**
         * this method pertains to servicing a refresh request for the list of rooms.
         * @param in input reader
         * @param out output writer
         * @throws IOException
         * @throws NumberFormatException
         */
        public void serviceRefreshRequest(BufferedReader in, PrintWriter out) throws IOException, NumberFormatException {
            // already in a try_catch from where this method was called from...
            String requestLine = "REFRESH\n";
            out.write(requestLine);

            String line = in.readLine(); // first line is "BEGIN + <roomCount>"; use this to initialize a temp String[].
            int roomCount = Integer.parseInt(line.split(" ")[1]);
            ArrayList<String> temp = new ArrayList<String>(roomCount);
            while (true) {
                line = in.readLine();
                if (line.equals("DONE")) {
                    break; // once we read "DONE", the full list has been sent.
                }
                temp.add(line);
            }
            /**
             * at this point, we have the current set of rooms; now we can 
             * compare with the local CSV ArrayList and update the table's model accordingly.
             */
            int i = 0, j = 0, csvLen = csvRoomDataObjs.size(), latestLen = roomCount;

            while (i < csvLen && j < latestLen) {
                if (csvRoomDataObjs.get(i).equals(temp.get(i))) {
                    i++;
                    j++;
                }
                else {
                    // a mismatch tells us that a room removal has occurred.
                    table.removeEntry(i);
                    csvRoomDataObjs.remove(i);
                }
            }
            // add any newly added rooms caught by the refresh.
            while (j < latestLen) {
                String[] entry = temp.get(j).split(",");
                table.addEntry(entry);
                csvRoomDataObjs.add(temp.get(j));
                j++;
            }
            // algorithm done!
        }

        /**
         * this thread's main line of execution.
         */
        public void run() {
            Socket socket;
            BufferedReader in;
            PrintWriter out;

            isRunning = true;
            try {
                socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                // send in the request line.
                String requestLine = Constants.LIST_ROOMS_REQ + '\n';
                out.write(requestLine);
                out.flush();
                /**
                 * message protocol...
                 * "BEGIN"
                 * <lines of room list data, one per line>
                 * "DONE"
                 */
                String line = in.readLine();
                if (line.equals("BEGIN")) {
                    System.out.println("Room list stream beginning... So far so good.");
                }
                while (true) {
                    line = in.readLine();
                    if (line.equals("DONE")) {
                        break; // if line reads "DONE", break.
                    }
                    // otherwise, read the data, feed it into the table, and store in CSV as a backup.
                    String[] dataArgs = line.split(",");
                    table.addEntry(dataArgs);
                    csvRoomDataObjs.add(line);
                }
                // principal list fetch complete; wait on user for any Refresh requests.
                while (true) {
                    workerNotify.wait();
                    if (!isRunning) {
                        break; // work is done, time to break.
                    }
                    // otherwise, we can rightfully assume user is requesting a refresh.
                    serviceRefreshRequest(in, out);
                }
                out.close();
                in.close();
                socket.close();

            }
            catch (Exception e) {
                System.out.println("RLH Error! --> " + e.getMessage());
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
