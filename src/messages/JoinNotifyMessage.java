package messages;

/**
 * sent to users of a chat room to notify them when another user has joined the
 * room.
 * 
 * Typically sent by the SessionCoordinator to be received by ChatUsers.
 */
public class JoinNotifyMessage extends Message {

    private String userThatJoined; // alias of the user that just joined
    private String roomJoined; // name of the room being joined

    /**
     * JNM constructor.
     * 
     * @param user alias of the user that just joined
     * @param room name of the room being joined
     */
    public JoinNotifyMessage(String user, String room) {
        super();
        userThatJoined = user;
        roomJoined = room;
    }

    /**
     * getter for the alias of the joined user.
     * 
     * @return alias of the user that just joined.
     */
    public String getUserJoined() {
        return userThatJoined;
    }

    /**
     * getter for the name of the room being joined.
     * 
     * @return name of the room being joined.
     */
    public String getRoomJoined() {
        return roomJoined;
    }

    /**
     * used to print the content of the message.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + userThatJoined + " has joined the room.";
    }

    /**
     * there is no associated sender alias, as these are sent by Coordinators, so
     * the purpose of this method is moot.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }
}
