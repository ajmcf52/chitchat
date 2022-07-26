package worker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import messages.ExitRoomMessage;
import messages.SimpleMessage;
import misc.ValidateInput;
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
        // info gathering.
        String roomName = userRef.getCurrentRoomName();
        String alias = userRef.getAlias();

        // building the ERM from info gathered.
        ExitRoomMessage erm = new ExitRoomMessage(alias, roomName);

        Socket socket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Object obj = null;
        String sessionIP = userRef.getSessionIP();
        int sessionPort = userRef.getSessionPort();
        try {
            socket = new Socket(sessionIP, sessionPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(erm);
            out.flush();
            obj = in.readObject();

            /**
             * nothing to do with this message; we read for the simple matter of ensuring
             * our message was read and replied to.
             */
            SimpleMessage response = ValidateInput.validateSimpleMessage(obj);

            socket.close();
        } catch (Exception e) {
            System.out.println("ERW error leaving room! --> " + e.getMessage());
        }
    }
}
