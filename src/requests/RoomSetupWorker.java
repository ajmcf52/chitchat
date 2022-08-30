package requests;

import java.net.Socket;

import misc.Constants;
import misc.ValidateInput;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.ChatUser;
import main.AppStateValue;
import main.ApplicationState;
import misc.Worker;

import messages.NewRoomMessage;
import messages.SimpleMessage;

/**
 * This thread-based class is responsible for communicating with the
 * Registry to get a new chat room set up for a given ChatUser, as
 * denoted by the user alias.
 * 
 */
public class RoomSetupWorker extends Worker {
    
    private ChatUser chatUser; // ChatUser we are setting up as the intended host.
    private Object chatUserLock; // ChatUser will wait to be notified on this (signifies work has been done)
    private ApplicationState appState; // modified to let main() know where we are at.
    private String roomName; // desired name of the room

    /**
     * constructor.
     * @param nameOfRoom number unique to the worker within its class of workers
     * @param ali user alias
     * @param chatLock notified to alert ChatUser in main() of progress.
     * @param state for main() loop control.
     */
    public RoomSetupWorker(String nameOfRoom, ChatUser chUser, Object chatLock, ApplicationState state) {
        super("RWS-0");
        roomName = nameOfRoom;
        chatUser = chUser;
        chatUserLock = chatLock;
        appState = state;
    }

    /**
     * This thread's main line of execution.
     */
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
            // NOTE order of constructor calls is crucial here! Reference ChatUser.java for more details.
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // write the NewRoomMessage to Registry.
            NewRoomMessage nrm = new NewRoomMessage(chatUser.getAlias(), roomName);
            out.writeObject(nrm);
            out.flush();

            /* NOTE Registry response is expected to be a SimpleMessage whose content follows
            the following format:

            "OK; ConnectInfo is IP:port"
            */

            Object obj = in.readObject();
            SimpleMessage response = ValidateInput.validateSimpleMessage(obj);

            // perform message processing here.
            String[] msgArgs = response.getContent().split(";");
            msgArgs = msgArgs[1].substring(1).split(" "); // msgArgs[1] --> " ConnectInfo is IP:port"
            String[] ipAndPort = msgArgs[2].split(":"); // msgArgs[2] --> "IP:port"
            String seshIp = ipAndPort[0];
            int seshPortNum = Integer.valueOf(ipAndPort[1]);
            chatUser.initSessionInfo(seshIp, seshPortNum, roomName); // perform ChatUser initialization with the Session.
            chatUser.setHost(true); // NOTE this is crucial for ensuring the chat thread is started in "CHATTING"
            appState.setAppState(AppStateValue.CHATTING);
            
            synchronized (chatUserLock) {
                chatUserLock.notify(); // allows ChatUser to proceed to the "CHATTING" state in its state machine.
            }

        } catch (Exception e) {
            System.out.println("RoomSetupWorker Exception --> " + e.getMessage());
        }
    }
}
