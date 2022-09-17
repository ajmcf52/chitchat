package messages;

/**
 * this class is sent by JoinRoomWorkers to the SessionCoordinator on behalf of
 * ChatUsers wishing to join the room that the SC is in charge of.
 */
public class JoinRoomMessage extends Message {
    private String userAlias; // alias of the user requesting to join the room.
    private String roomName; // name of the room requested to be joined.

    /**
     * JRM constructor.
     * 
     * @param user name of the user (i.e., alias)
     * @param room name of the room that user is requesting to join
     */
    public JoinRoomMessage(String user, String room) {
        super();
        userAlias = user;
        roomName = room;
    }

    /**
     * getter for the alias of the user requesting to join.
     * 
     * @return alias of user requesting to join.
     */
    public String getUserJoining() {
        return userAlias;
    }

    /**
     * getter for the name of the room being requested for joining.
     * 
     * @return name of the room the user wishes to join.
     */
    public String getRoom() {
        return roomName;
    }

    /**
     * for debugging purposes only. no user-facing application here.
     * 
     * @return String-based message that can be used to debug the app.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + userAlias + " is requesting to join " + roomName + ".";
    }

    /**
     * getter for the associated sender alias of this message, which is the ChatUser
     * being represented by the JoinRoomWorker sending the message.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return getUserJoining();
    }

}
