package messages;

public class NewUserMessage extends Message {
    private String alias; // name of the new user.

    /**
     * NUM constructor.
     * @param a name of the new user
     */
    public NewUserMessage(String a) {
        alias = a;
    }

    // getter for user alias.
    public String getAlias() { return alias; }

    /**
     * for debugging purposes only.
     * @return String-based text describing contents of the message.
     */
    @Override
    public String print() {
        return alias + " has joined the system.";
    }
}
