package messages;

import java.util.ArrayList;

/**
 * when sent from a RoomsListFetcher to a Registry's RequestHandler, this
 * message acts as a "Request" for a listing of current rooms.
 * 
 * when it is sent from a RequestHandler BACK to an RLF, this message will
 * contain the room listings in the form of an ArrayList<String>, where each
 * String adheres to the following CSV format:
 * 
 * "room name,host name, # of guests,<ip>:<port number>"
 */
public class ListRoomsMessage extends Message {

    private ArrayList<String> roomDataCsvList; // list of CSV-style room data listings (outlined above)

    /**
     * LRM constructor. Respondent may use the setter to initialize the room
     * listings.
     */
    public ListRoomsMessage() {
        roomDataCsvList = null;
    }

    /**
     * setter for room listings.
     * 
     * @param listings current list of rooms that can be joined.
     */
    public void setListings(ArrayList<String> listings) {
        roomDataCsvList = listings;
    }

    /**
     * getter for room listings.
     * 
     * @return current list of rooms available to be joined.
     */
    public ArrayList<String> getListings() {
        return roomDataCsvList;
    }

    /**
     * can be used to debug.
     * 
     * @return String representation of the listings.
     */
    @Override
    public String getContent() {
        String result = getFormattedStamp() + " Requested Room Listings Below:\n\n";
        for (String s : roomDataCsvList) {
            result += s + '\n';
        }
        return result;
    }

    /**
     * The purpose of this method is moot. Here to make the compiler happy.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }

}
