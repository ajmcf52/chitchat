package messages;

import java.io.Serializable;

import misc.TimeStampGenerator;

/**
 * a representation of information currency that is passed around between
 * various communicative entities within the application.
 */
public abstract class Message implements Serializable {

    /**
     * used in message-routing logic to determine which users should receive which
     * messages. SingleShot == false means that everyone BUT the associated alias of
     * a Message is to receive said Message.
     * 
     * SingleShot == true means that only one user is intended to receive this
     * Message. For instance, WelcomeMessages should only be sent to the associated
     * receiving user, whereas every other user in the chat should receive a
     * JoinNotifyMessage, implying that JNMs would have singleShot set to false.
     */
    private boolean isSingleShot;

    private String timestamp; // time at which this message was created.

    /**
     * Message constructor.
     */
    Message() {
        timestamp = TimeStampGenerator.now();
        isSingleShot = false;
    }

    /**
     * getter for untampered timestamp.
     * 
     * @return nonformatted timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * getter for formatted timestamp.
     * 
     * @return timestamp, surrounded by a pair of square brackets.
     */
    public String getFormattedStamp() {
        return "[" + timestamp + "]";
    }

    /**
     * setter for single shot. No argument required. boolean defaults to false, so
     * this method should only ever be called to set the value to true.
     */
    public void markSingleShot() {
        isSingleShot = true;
    }

    /**
     * getter for isSingleShot.
     * 
     * @return whether or not singleShot is set.
     */
    public boolean isSingleShot() {
        return isSingleShot;
    }

    /**
     * A simple print method. to be implemented by all subclasses. bear in mind
     * that, to some subclasses, this method will be absolutely useless, thus the
     * implementation will serve nothing more than compiler satisfaction.
     * 
     * @return associated String-based message to be printed.
     */
    public abstract String getContent();

    /**
     * Getter for the associated alias of this Message's sender. There will be some
     * cases where this returns "" when there is no meaningful associated sender
     * (i.e., SessionCoordinator).
     * 
     * @return associated message sender's alias
     */
    public abstract String getAssociatedSenderAlias();
}
