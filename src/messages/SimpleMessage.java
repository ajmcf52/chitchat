package messages;

/**
 * a simple text-based message.
 * used mostly by users to send and receive text-based,
 * chatroom-centric messages. Is also sometimes used
 * by backend-centric workers to communicate simple things,
 * such as acknowledgement to a JoinRoomMessage.
 */
public class SimpleMessage extends Message {

    private String alias;
    private String text;

    /**
     * SM constructor.
     * @param a user alias for whom this message is concerned
     * @param t text content of the message
     */
    public SimpleMessage(String a, String t) {
        alias = a;
        text = t;
    }

    // getter for alias
    public String getAssociatedAlias() { return alias; }

    /**
     * simple print function for accessing
     * the articulated meaning of this message.
     * @return text of the message
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + text;
    }
    
}
