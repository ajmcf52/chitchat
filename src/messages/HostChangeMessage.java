package messages;

/**
 * used by the SessionCoordinator to communicate a host change to others in the
 * room, typically right after another user has just left said room.
 */
public class HostChangeMessage extends Message {

    private String newHost; // alias of the new room host
    private boolean isToNewHost; // true if message is meant for new host, false otherwise.

    /**
     * HCM constructor.
     * 
     * @param hostName  name of the host-to-be.
     * @param toNewHost true if message is being sent to the new host, false
     *                      otherwise.
     */
    public HostChangeMessage(String hostName, boolean toNewHost) {
        super();
        newHost = hostName;
        isToNewHost = toNewHost;
        if (isToNewHost)
            markSingleShot();
    }

    /**
     * string representation of what it would look like to print the contents of
     * this Message out.
     */
    @Override
    public String getContent() {
        return isToNewHost ? getFormattedStamp() + " You are now the host of this room."
                        : getFormattedStamp() + " " + newHost + " is now the host of this room.";
    }

    /**
     * sender, in any case, is the SessionCoordinator, and so we leave this blank.
     */
    @Override
    public String getAssociatedSenderAlias() {
        return "";
    }

}
