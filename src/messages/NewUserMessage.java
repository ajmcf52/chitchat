package messages;

/**
 * sent by UserSetupWorkers and received by the Registry to facilitate the
 * instantiation and setup of a new ChatUser.
 */
public class NewUserMessage extends Message {

    private String alias; // name of the new user.

    /**
     * NUM constructor.
     * 
     * @param a name of the new user
     */
    public NewUserMessage(String a) {
        super();
        alias = a;
    }

    /**
     * getter for alias.
     * 
     * @return user alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * for debugging purposes only.
     * 
     * @return String-based text describing contents of the message.
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + alias + " has joined the system.";
    }

    /**
     * getter for sender alias.
     * 
     * @return sender alias
     */
    @Override
    public String getAssociatedSenderAlias() {
        return getAlias();
    }
}
