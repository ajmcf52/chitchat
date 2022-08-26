package messages;

/**
 * the first message that is received by a ChatUser
 * upon entering a chat room. 
 * Typically sent by a SessionCoordinator & received by a ChatUser.
 */
public class WelcomeMessage extends Message {
    private String userJoining; // alias of the user joining the room.
    private String roomJoined; // name of room that has been joined. Used to set chat window title.
    private String text; // content of the welcome message.

    /**
     * WM constructor.
     * @param user alias of the user joining
     * @param roomName name of the room being joined.
     * @param t text content of the welcome message.
     */
    public WelcomeMessage(String user, String roomName, String t) {
        userJoining = user;
        roomJoined = roomName;
        text = t;
    }

    /**
     * a simple print function.
     * @return String-based message to be printed.
     */
    public String print() {
        return "Welcome, " + userJoining + "! You have joined " + roomJoined + ".";
    }
}
