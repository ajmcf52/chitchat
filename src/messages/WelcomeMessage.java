package messages;

/**
 * the first message that is received by a ChatUser
 * upon entering a chat room. 
 * Typically sent by a SessionCoordinator & received by a ChatUser.
 */
public class WelcomeMessage extends Message {
    private String userJoining; // alias of the user joining the room.
    private String roomJoined; // name of room that has been joined. Used to set chat window title.
    private boolean isHost; // true if the user joining is the host of their room, false otherwise.

    /**
     * WM constructor.
     * @param user alias of the user joining
     * @param roomName name of the room being joined
     * @param hosting indicates whether or not this user will be host of the room.
     */
    public WelcomeMessage(String user, String roomName, boolean hosting) {
        super();
        userJoining = user;
        roomJoined = roomName;
        isHost = hosting;
    }

    // getters
    public String getAssociatedAlias() { return userJoining; }
    public String getAssociatedRoomName() { return roomJoined; }
    public boolean isHosting() { return isHost; }

    /**
     * a simple print function. Contents printed depend on whether or not the
     * user being greeted is the host of the room or not.
     * 
     * @return String-based message to be printed.
     */
    public String getContent() {
        return isHost ? getFormattedStamp() + " Welcome, " + userJoining + ". You are the host of this room." : 
        getFormattedStamp() + " Welcome, " + userJoining + ". You have joined " + roomJoined + ".";
    }
}
