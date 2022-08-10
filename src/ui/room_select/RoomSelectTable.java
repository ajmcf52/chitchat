package ui.room_select;

import java.util.ArrayList;
import javax.swing.JTable;

/**
 * this class will be used to house and display information for 
 * each joinable chat room in a table-wise format.
 */
public class RoomSelectTable extends JTable {


    
    private ArrayList<String[]> entries; // 2D dynamic array of table entries
    private static final String[] COLUMNS = {"Room Name",  "Host Name", "# of Guests", "<IP>:<Port>"}; // names of each of the columns 

    /**
     * constructor for RST.
     */
    public RoomSelectTable() {
        entries = new ArrayList<String[]>();
    }

    /**
     * method for adding a new line of data to the room select table.
     * (called when a new room is created)
     * @param args ordered room details in a String array.
     */
    public void addEntry(String[] args) {
        if (args.length != COLUMNS.length) {
            System.out.println("Args length mismatch! Returning..");
            return;
        }
        entries.add(args);
    }

    /**
     * used ot clear all entries.
     */
    public void clear() {
        entries.clear();
    }
}
