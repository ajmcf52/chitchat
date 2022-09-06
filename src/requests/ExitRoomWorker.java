package requests;

import messages.ExitRoomMessage;
import net.ChatUser;

/**
 * worker responsible for beginning the exit process for ChatUsers leaving a
 * chat room.
 */
public class ExitRoomWorker extends Thread {

    private ChatUser userRef; // a reference to the chat user.

    /**
     * ERW constructor.
     * 
     * @param user ChatUser object
     */
    public ExitRoomWorker(ChatUser user) {
        userRef = user;
    }

    public void run() {
        String roomName = userRef.getCurrentRoomName();
        String alias = userRef.getAlias();
        ExitRoomMessage erm = new ExitRoomMessage(alias, roomName);
        userRef.pushOutgoingMessage(erm);
    }
}
