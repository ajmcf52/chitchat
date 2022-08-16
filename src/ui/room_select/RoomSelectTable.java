package ui.room_select;

import java.lang.IndexOutOfBoundsException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * this class will be used to house and display information for 
 * each joinable chat room in a table-wise format.
 */
public class RoomSelectTable extends JTable {

    private static final String[] COLUMNS = {"Room Name",  "Host Name", "# of Guests", "<IP>:<Port>"}; // names of each of the columns 
    private DefaultTableModel model; // table model
    /**
     * constructor for RST.
     */
    public RoomSelectTable() {
        model = new DefaultTableModel();
        for (int i = 0; i < COLUMNS.length; i++) {
            model.addColumn(COLUMNS[i]);
        }
        String[] args = {"farts", "table", "andy", "testing"};
        this.setModel(model);
        this.addEntry(args);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
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
        model.addRow(args);
    }

    /**
     * this method is used to remove the entry at a particular index in the list of entries.
     * @param i index of entry to be removed
     * @throw IndexOutOfBoundsException
     */
    public void removeEntry(int i) throws IndexOutOfBoundsException {
        if (i < 0 || i >= model.getRowCount()) {
            System.out.println("Invalid index supplied to removeEntry(i) in RST.java");
            throw new IndexOutOfBoundsException();
        }
        model.removeRow(i);
    }

    /**
     * used ot clear all entries.
     */
    // public void clear() {
    //     model.
    // }
}
