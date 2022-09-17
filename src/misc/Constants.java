package misc;

/**
 * this class contains some global constants to be imported and used by various
 * entities across the application.
 * 
 * Some
 */
public final class Constants {

    public static final String TIMEZONE = "Canada/Pacific";
    public static final int HOST_INDEX = 0;
    public static final String CLIENT_IP = "localhost";
    public static final String REGISTRY_IP = "localhost";
    public static final String COORDINATOR_IP = "localhost";
    public static final int MSG_QUEUE_LENGTH = 10; // length of any given message queue in the application.
    public static final int REGISTRY_PORT = 8000;
    public static final int SESSION_PORT_PREFIX = 9000; // 9000, 9001, 9002, ..., etc
    public static final int KC_RETURN = 10; // keycode of "Enter"
    public static final String UID_PREFIX = "U0";
    public static final String SID_PREFIX = "S0";

    /**
     * column numbers for accessing values in the RoomSelectTable data.
     */
    public static final int ROOM_NAME_TABLE_COLUMN = 0;
    public static final int GUEST_COUNT_TABLE_COLUMN = 2;
    public static final int IP_PORT_TABLE_COLUMN = 3;

    /**
     * [RecordSeparator] special ASCII character, impossible to type. Great
     * delimiter.
     * 
     * Reason for using this is so incoming messages that include user-supplied
     * characters can be separated from other information without compromising what
     * the user sent.
     * 
     * For example, if we used " " or "," as a delimiter for messages that include
     * user text, messages would become inevitably compromised. By using a delimiter
     * that cannot (to my knowledge) be typed by a user on the keyboard (at least in
     * reasonable circumstances), we maintain integrity of user-supplied messages
     * sent across the network to other users.
     * 
     * NOTE it's funny because as clever as I thought this was, I went away with
     * delimiters and String-based messages, moving over to Message objects, which
     * ended up being much easier to work with and debug.
     */
    public static final String DELIM = Character.toString((char) 0x1E);
    public static final int SPACE_ASCII = 32;
    public static final int TILDE_ASCII = 126;

    /**
     * used for input validation for alias and room naming.
     */
    public static final int MIN_USER_INPUT_LENGTH = 2;
    public static final int MAX_USER_INPUT_LENGTH = 16;

}
