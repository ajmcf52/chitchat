package messages;

import java.io.Serializable;

import misc.TimeStampGenerator;

/**
 * an abstract serializable class that will be implemented by all
 * subclassing message types.
 */
public abstract class Message implements Serializable {

    String stamp; // time at which this message was created

    /**
     * empty constructor.
     */
    Message() {
        stamp = TimeStampGenerator.now();
    }

    // timestamp getters.
    public String getTimestamp() { return stamp; }
    public String getFormattedStamp() { return "[" + stamp + "]"; }

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
