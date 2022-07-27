package main;

/**
 * this class contains some global constants to be imported and used by various entities across the network.
 */
public final class Constants {
    /**
     * unused constructor.
     */
    private Constants() {}
    public static final String CLIENT_IP = "localhost";
    public static final String REGISTRY_IP = "localhost";
    public static final int CLIENT_PORT = 9006;
    public static final int REGISTRY_PORT = 8000;
    public static final int KC_RETURN = 10;
    public static final String UID_PREFIX = "U0";
    public static final String NEW_USER_REQ = "NEW_USER_REQUEST"; // signifies a new user request.
}
