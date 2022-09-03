package misc;

/**
 * the intent of this class is to serve as a simple superclass for all the
 * different types of worker bee thread-based classes that we have within the
 * app, eliminating code redundancy with a few of the common things that all of
 * them should have:
 * 
 * i) a worker ID of some sort ii) shut-off signalling capability
 * 
 * more desired functionality may arise with time, but for now, i) and ii) is
 * all that can be thought of.
 */
public abstract class Worker extends Thread {
    protected String workerID; // unique to each worker
    protected volatile boolean isRunning; // boolean flag
    protected final Object runLock = new Object(); // lock for synchronizing on above flag

    /**
     * constructor for Worker.
     * 
     * @param wid worker ID string
     */
    public Worker(String wid) {
        workerID = wid;
        isRunning = false;
    }

    /**
     * This method is called to turn the worker on.
     */
    public void turnOn() {
        isRunning = true;
    }

    /**
     * this method is called to turn the worker off. We synchronize here, as this
     * method will likely be called by an entity other than the thread itself, and
     * the thread itself must read the boolean every loop cycle to determine whether
     * or not it should exit.
     * 
     * We have a race condition during instances where an external thread tries to
     * shut off the Worker as it is in the process of reading the variable.
     */
    public void turnOff() {
        synchronized (runLock) {
            isRunning = false;
        }
    }

    /**
     * getter method for WID.
     * 
     * @return workerID
     */
    public String getID() {
        return workerID;
    }

    public void proclaimShutdown() {
        System.out.println(workerID + " received signal to turn off. Shutting down now.");
    }

}
