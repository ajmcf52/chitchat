package messages;

/**
 * a simple text-based message. used mostly by users to send and receive
 * text-based, chatroom-centric messages. Is also sometimes used by
 * backend-centric workers to communicate simple things, such as acknowledgement
 * of prior message reception.
 */
public class SimpleMessage extends Message {

    private String alias; // associated sender alias.
    private String text; // message content.

    /**
     * SM constructor.
     * 
     * @param a user alias for whom this message is concerned
     * @param t text content of the message
     */
    public SimpleMessage(String a, String t) {
        super();
        alias = a;
        text = t;
    }

    /**
     * formatted accessor for text content of the message.
     * 
     * @return formatted content of the message
     */
    @Override
    public String getContent() {
        return getFormattedStamp() + " " + alias + ": " + text;
    }

    /**
     * getter for alias.
     * 
     * @return sender alias
     */
    @Override
    public String getAssociatedSenderAlias() {
        return alias;
    }

}
