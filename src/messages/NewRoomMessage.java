package messages;

/**
 * signifies the desire for a new room to be created.
 * 
 * typically sent by a RoomSetupWorker & received by the Registry.
 */
public class NewRoomMessage extends Message {
    private String hostAlias; // alias of the user requesting the new room; soon-to-be room host.
    private String roomName; // requested name of the room-to-be.

    /**
     * NRM constructor.
     * @param host alias of the user requesting to create the room.
     * @param room requested name of the room.
     */
    public NewRoomMessage(String host, String room) {
        super();
        hostAlias = host;
        roomName = room;
    }

    // getters
    public String getHost() { return hostAlias; }
    public String getRoomName() { return roomName; }

    /**
     * Used for debugging purposes on the side of the Registry.
     * No user-facing application of this method here.
     * @return informative debugging stuff.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + hostAlias + " is requesting to create a room by the name of " + roomName + ".";
    }
    
}
