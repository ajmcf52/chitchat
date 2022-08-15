package misc;

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
}
