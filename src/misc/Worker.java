package misc;

/**
 * this class represents a thread-based worker entity, all subclasses having
 * 
 * i) a worker ID of some sort, and ii) shut-off signalling capability
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

    /**
     * useful for communicating when workers shut down with console printing.
     */
    public void proclaimShutdown() {
        System.out.println(workerID + " received signal to turn off. Shutting down now.");
    }

}
