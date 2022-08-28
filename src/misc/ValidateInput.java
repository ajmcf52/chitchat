package misc;

import messages.ListRoomsMessage;
import messages.Message;
import messages.SimpleMessage;

/**
 * collection of user input validation methods.
 */
public interface ValidateInput {
    /**
     * used to validate a user-supplied string.
     * @param str input being validated
     * @param minLen minimum length
     * @param maxLen maximum length
     * @return true if valid, false otherwise.
     */
    public static boolean validateLength(String str, int minLen, int maxLen) {
        int strLen = str.length();
        if (strLen < minLen || strLen > maxLen) {
            return false;
        }
        return true;
    }

    /**
     * used to validate that a string is strictly alphanumeric.
     * @param str
     */
    public static boolean validateAlphaNumeric(String str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if ((str.charAt(i) < 'A' && str.charAt(i) > '9') ||
            (str.charAt(i) < 'a' && str.charAt(i) > 'Z') || 
            str.charAt(i) > 'z' || str.charAt(i) < '0' ) {
                return false;
            }
        }
        return true;
    }

    /**
     * used to validate a string whose characters are generically visible and non-special
     * (i.e., characters that fall within the ASCII range of 32 to 126 inclusive)
     * @param str
     * @return
     */
    public static boolean validateGeneric(String str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (str.charAt(i) < Constants.SPACE_ASCII || str.charAt(i) > Constants.TILDE_ASCII) {
                return false;
            }
        }
        return true;
    }

    /**
     * simple message validation function for message streamers.
     * Messages come in as Objects; this function is used to 
     * frisk and cast said Objects into Messages.
     * @param obj the Object in question
     * @return the casted Message
     * @throws ClassCastException
     */
    public static Message validateMessage(Object obj) throws ClassCastException {
        if (!(obj instanceof Message)) {
            ClassCastException e = new ClassCastException("Bad Cast from " + obj.toString() + " to Message!");
            e.printStackTrace();
            throw e;
        }
        return (Message) obj;
    }

    /**
     * message validation function for SimpleMessages.
     * @param obj the Object transmitted across the wire that we expect to be a SimpleMessage
     * @return the casted SimpleMessage
     * @throws ClassCastException
     */
    public static SimpleMessage validateSimpleMessage(Object obj) throws ClassCastException {
        if (!(obj instanceof SimpleMessage)) {
            ClassCastException e = new ClassCastException("Bad Cast from " + obj.toString() + " to SimpleMessage!");
            e.printStackTrace();
            throw e;
        }
        return (SimpleMessage) obj;
    }

    public static ListRoomsMessage validateListRoomsMessage(Object obj) throws ClassCastException {
        if (!(obj instanceof ListRoomsMessage)) {
            ClassCastException e = new ClassCastException("Bad Cast from " + obj.toString() + " to ListRoomsMessage!");
            e.printStackTrace();
            throw e;
        }
        return (ListRoomsMessage) obj;
    }
 
}
