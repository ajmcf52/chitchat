package messages;

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
        userExiting = user;
        roomName = room;
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
