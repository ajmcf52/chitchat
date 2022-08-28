package messages;

/**
 * sent to users of a chat room to notify them
 * when another user has joined the room.
 * 
 * Typically sent by SessionCoordinator (via MessageRouter),
 * and received directly by ChatUsers.
 */
public class JoinNotifyMessage extends Message {

    private String userThatJoined; // alias of the user that just joined
    private String roomJoined; // name of the room being joined

    /**
     * JNM constructor.
     * @param user alias of the user that just joined
     * @param room name of the room being joined
     */
    public JoinNotifyMessage(String user, String room) {
        super();
        userThatJoined = user;
        roomJoined = room;
    }

    // getters
    public String getUserJoined() { return userThatJoined; }
    public String getRoomJoined() { return roomJoined; }

    /**
     * used to print the content of the message.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + userThatJoined + " has joined the room.";
    }

    /**
     * given that this class is a tad special, as the sending of Message types
     * are typically triggered by a SeshCoordinator, there is no associated
     * sender alias.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }
}
