package messages;

/**
 * initially sent from ExitRoomWorkers (on behalf of a ChatUser requesting leave
 * from a chat room) to receiving SessionCoordinators.
 * 
 * When a ChatUser receives one back from a SessionCoordinator, this acts as
 * acknowledgement for the exit request.
 */
public class ExitRoomMessage extends Message {

    private String userExiting; // name (i.e., alias) of the user exiting the room
    private String roomName; // name of the room being exited

    /**
     * ERM constructor
     * 
     * @param user name of the user exiting the room
     * @param room name of the room being exited
     */
    public ExitRoomMessage(String user, String room) {
        super();
        userExiting = user;
        roomName = room;
        markSingleShot(); // NOTE this message type will never be broadcast.
    }

    /**
     * getter for the user requesting exit.
     * 
     * @return name of the user requesting exit the room
     */
    public String getExitingUser() {
        return userExiting;
    }

    /**
     * getter for the room being exited.
     * 
     * @return name of the room being exited
     */
    public String getAssociatedRoom() {
        return roomName;
    }

    /**
     * for debugging purposes.
     * 
     * @return informative String indicating the user that is leaving
     */
    @Override
    public String getContent() {
        return userExiting + " wishes to leave " + roomName + ".";
    }

    /**
     * sender in this case is the user that is exiting.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return userExiting;
    }

}
