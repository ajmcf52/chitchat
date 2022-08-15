package misc;

/**
 * this class contains a complete set of all
 * the potential requests that the Registry 
 * can expect to receive at any given time.
 */
public class Requests {
    public static final String NEW_USER_REQ = "NEW_USER_REQUEST"; // new user request.
    public static final String NEW_ROOM_REQ = "NEW_ROOM_REQUEST"; // request for a new chat room.
    public static final String LIST_ROOMS_REQ = "LIST_ROOMS_REQUEST"; // request for a complete list of available rooms.
    public static final String JOIN_ROOM_REQ = "JOIN_ROOM_REQUEST"; // join existing room request (i.e., Bob joining Alice's Room)
    public static final String LEAVE_ROOM_REQ = "LEAVE_ROOM_REQUEST"; // leave request for current room (i.e., Bob leaving Alice's Room)
}
