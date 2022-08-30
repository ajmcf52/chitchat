package messages;

import java.util.ArrayList;

/**
 * the first message that is received by a ChatUser
 * upon entering a chat room. 
 * Typically sent by a SessionCoordinator & received by a ChatUser.
 */
public class WelcomeMessage extends Message {
    private String userJoining; // alias of the user joining the room.
    private String roomJoined; // name of room that has been joined. Used to set chat window title.
    private boolean isHost; // true if the user joining is the host of their room, false otherwise.
    private ArrayList<String> participants; // users that are already in the room (can be empty)

    /**
     * WM constructor.
     * @param user alias of the user joining
     * @param roomName name of the room being joined
     * @param hosting indicates whether or not this user will be host of the room
     */
    public WelcomeMessage(String user, String roomName, boolean hosting) {
        super();
        userJoining = user;
        roomJoined = roomName;
        isHost = hosting;
        participants = new ArrayList<>();
        markSingleShot(); // NOTE All WelcomeMessages are single shot! See Message.java for more details.
    }

    /**
     * additional WM constructor. Offers the option to include a list of participants.
     * @param user alias of the user joining
     * @param roomName name of room being joined
     * @param hosting boolean indicating whether or not the user joining will be room host
     * @param p list of participants
     */
    public WelcomeMessage(String user, String roomName, boolean hosting, ArrayList<String> p) {
        this(user, roomName, hosting);
        participants = p;
    }

    // getters
    public String getAssociatedReceivingAlias() { return userJoining; }
    public String getAssociatedRoomName() { return roomJoined; }
    public boolean isHosting() { return isHost; }
    public ArrayList<String> getParticipants() { return participants; }

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

    /**
     * NOTE sender alias here should not be userJoining, as technically,
     * the sender of this WelcomeMessage is SeshCoordinator; the RECEIVER is
     * userJoining. Hence, returning an empty string works just fine here.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }
}
