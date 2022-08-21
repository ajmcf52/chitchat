package misc;

/**
 * this class contains some global constants to be imported and used by various entities across the network.
 */
public final class Constants {
    
    public static final String TIMEZONE = "Canada/Pacific";
    public static final String WELCOME_TAG = "(WELCOME)";
    public static final int HOST_INDEX = 0;
    public static final String CLIENT_IP = "localhost";
    public static final String REGISTRY_IP = "localhost";
    public static final String COORDINATOR_IP = "localhost";
    public static final int MSG_QUEUE_LENGTH = 10; // length of any given message queue in the application.
    public static final int REGISTRY_PORT = 8000;
    public static final int SESSION_PORT_PREFIX = 9000; 
    public static final int KC_RETURN = 10;
    public static final String UID_PREFIX = "U0";
    public static final String SID_PREFIX = "S0";
    public static final String DELIM = Character.toString((char)0x1E); // [RecordSeparator] special ASCII character, impossible to type. Great delimiter.
    public static final int SPACE_ASCII = 32;
    public static final int TILDE_ASCII = 126;
    public static final int MIN_USER_INPUT_LENGTH = 2;
    public static final int MAX_USER_INPUT_LENGTH = 16;

    // these three constants are used to declare the chat state of a user in a ternary context.
    public static final int SOCKET_SETUP = -1;
    public static final int NOT_CHATTING = 0; 
    public static final int CHATTING = 1;
    
}
