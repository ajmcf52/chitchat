package messages;

import java.io.Serializable;

/**
 * an abstract serializable class that will be implemented by all
 * subclassing message types.
 */
public abstract class Message implements Serializable {
    /**
     * empty constructor.
     */
    Message() {}

    /**
     * a simple print method. to be implemented by all subclasses.
     * bear in mind that, to some subclasses, this method will be absolutely useless,
     * thus the implementation will serve nothing more than compiler satisfaction.
     * @return associated String-based message to be printed.
     */
    public abstract String print();
}
