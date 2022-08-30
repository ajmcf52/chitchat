package messages;

import java.io.Serializable;

import misc.TimeStampGenerator;

/**
 * an abstract serializable class that will be implemented by all
 * subclassing message types.
 */
public abstract class Message implements Serializable {

    /**
     * used in message-routing logic to determine which users
     * should receive which messages.
     * SingleShot == false --> everyone BUT the associated alias
     * of a Message is to receive said Message.
     * 
     * SingleShot == true --> only one user is intended to receive
     * this Message. For instance, WelcomeMessages should only be sent
     * to the associated receiving user, whereas every other user in
     * the chat should receive a JoinNotifyMessage [singleShot = false]
     */
    private boolean isSingleShot;

    private String stamp; // time at which this message was created

    /**
     * empty constructor.
     */
    Message() {
        stamp = TimeStampGenerator.now();
        isSingleShot = false;
    }

    // timestamp getters.
    public String getTimestamp() { return stamp; }
    public String getFormattedStamp() { return "[" + stamp + "]"; }

    /**
     * setter for single shot. No argument required. 
     * boolean defaults to false, so this method should
     * only ever be called to set the value to true.
     */
    public void markSingleShot() { isSingleShot = true; }

    // getter for isSingleShot
    public boolean isSingleShot() { return isSingleShot; }

    /**
     * a simple print method. to be implemented by all subclasses.
     * bear in mind that, to some subclasses, this method will be absolutely useless,
     * thus the implementation will serve nothing more than compiler satisfaction.
     * @return associated String-based message to be printed.
     */
    public abstract String getContent();

    /**
     * Getter for the associated alias of this Message's sender.
     * There will be some cases where this returns "". For example,
     * @return associated message sender's alias
     */
    public abstract String getAssociatedSenderAlias();
}
