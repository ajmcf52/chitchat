package misc;

/**
 * this class is used specifically as a mechanism for the RoomSelectPanel and
 * the RoomsListFetcher to communicate with one another during room validation.
 * Specifically, the object consists of two flags. The first one indicates
 * whether or not a room validation has been requested, the second indicating
 * whether the validation was successful or not.
 * 
 * In other words, the first flag is used to trigger the execution of a job,
 * whereas the second flag is used to communicate the result of said job.
 * 
 */
public class SharedValidateNotifier {
    private boolean validationRequested;
    private boolean validationSuccessful;

    public SharedValidateNotifier() {
        validationRequested = false;
        validationSuccessful = false;
    }

    /**
     * sets validation request flag to true.
     */
    public void toggleRequest() {
        validationRequested = true;
    }

    /**
     * sets the validation success flag to true.
     */
    public void markAsSuccessful() {
        validationSuccessful = true;
    }

    /**
     * getter for the requested flag.
     * 
     * @return validation requested flag
     */
    public boolean readRequested() {
        return validationRequested;
    }

    /**
     * getter for the validation successful flag.
     * 
     * @return validation successful flag
     */
    public boolean readSuccessful() {
        return validationSuccessful;
    }

    /**
     * resets the given flags.
     */
    public void reset() {
        validationRequested = false;
        validationSuccessful = false;
    }
}
