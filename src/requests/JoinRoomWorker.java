package requests;

import misc.Worker;

/**
 * this class performs the leg work of getting  
 * a user into another user's chat room.
 */
public class JoinRoomWorker extends Worker {
    
    /**
     * constructor for JRW
     * @param workerNum unique number identifying this worker within its worker class
     * @param connectInfo connection info for SessionCoordinator of the room being joined
     * @param userJoining name (i.e., alias) of the user requesting to join (i.e., Bob)
     */
    public JoinRoomWorker(int workerNum, String connectInfo, String userJoining) {
        super("JRW-" + Integer.toString(workerNum));
    }

    /**
     * this thread's main line of execution
     */
    public void run() {

    }
}
