package messages;

public class JoinRoomMessage extends Message {
    private String userAlias; // alias of the user requesting to join the room.
    private String roomName; // name of the room requested to be joined.

    /**
     * JRM constructor.
     * @param user name of the user (aka alias)
     * @param room name of the room that user is requesting to join 
     */
    public JoinRoomMessage(String user, String room) {
        userAlias = user;
        roomName = room;
    }

    /**
     * for debugging purposes only. no user-facing
     * application here.
     * @return String-based message that can be used to debug the app.
     */
    @Override
    public String print() {
        return userAlias + " is requesting to join " + roomName + ".";
    }
    
}
