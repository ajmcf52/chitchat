package messages;

/**
 * sent from the SessionCoordinator to users in a chat room as a way of keeping
 * them in the loop of a particular user having left the room.
 */
public class ExitNotifyMessage extends Message {
    private String userLeaving; // alias of the user that left
    private String roomBeingLeft; // room that is being left

    /**
     * ENM constructor.
     * 
     * @param user name (i.e., alias) of the user leaving
     * @param room name of the room being left
     */
    public ExitNotifyMessage(String user, String room) {
        super();
        userLeaving = user;
        roomBeingLeft = room;
    }

    /**
     * getter for user in the midst of leaving.
     * 
     * @return name of user leaving
     */
    public String getUserLeaving() {
        return userLeaving;
    }

    /**
     * getter for room being left.
     * 
     * @return name of the room being left
     */
    public String getRoomLeft() {
        return roomBeingLeft;
    }

    /**
     * Content of the print statement that will be directly inserted into users'
     * chat feeds.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + userLeaving + " has left the room.";
    }

    /**
     * sender is technically the SessionCoordinator, and so we can leave this blank.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }

}
