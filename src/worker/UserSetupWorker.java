package worker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import misc.Constants;
import misc.ValidateInput;
import net.ChatUser;
import main.AppStateValue;
import main.ApplicationState;
import misc.Worker;
import messages.NewUserMessage;
import messages.SimpleMessage;

/**
 * This class is responsible for setting up the ChatUser with its properties,
 * namely its alias and UID. The alias is passed in through the LoginPanel's
 * KeyEventHandler. UST fetches the UID from the registry.
 */
public class UserSetupWorker extends Worker {

    private String alias;
    private ChatUser userRef;
    private Object chatUserLock;
    private ApplicationState appState;

    /**
     * constructor for the user setup worker.
     * 
     * @param workerNum number unique to this worker within its class
     * @param ali       String-based alias to be set as the chat user's screen name
     * @param cu        chat user object reference
     * @param cuLock    chat user lock (notify on this for crucial events)
     * @param state     app state
     */
    public UserSetupWorker(int workerNum, String ali, ChatUser cu, Object cuLock, ApplicationState state) {
        super("USW-" + Integer.toString(workerNum));
        alias = ali;
        userRef = cu;
        chatUserLock = cuLock;
        appState = state;
    }

    /**
     * the UserSetupThread's main course of action.
     * 
     * NOTE format of the SimpleMessage response will be: "OK; UID is <uid>"
     */
    public void run() {
        Socket socket = null;

        try {
            socket = new Socket(Constants.REGISTRY_IP, Constants.REGISTRY_PORT);
            // NOTE order of constructor calls is crucial here! Reference ChatUser.java for
            // more details.
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            // send the protocol message on one line, then the alias on following line.
            NewUserMessage msg = new NewUserMessage(alias);
            out.writeObject(msg);
            out.flush();

            Object obj = in.readObject(); // should be a SimpleMessage containing the UID string for the user
            SimpleMessage response = ValidateInput.validateSimpleMessage(obj);

            // initialize the ChatUser's fields.
            userRef.initializeID(response, alias);
            // work is done! Prepare for exit, and modify app state accordingly.

            socket.close(); // NOTE this will close both associated streams.
            appState.setAppState(AppStateValue.CHOICE_PANEL);

            synchronized (chatUserLock) {
                chatUserLock.notify(); // allows ChatUser to go to the top of its state machine & enter ChoicePanel.
            }
        } catch (Exception e) {
            System.out.println("UserSetupThread error! --> " + e.getMessage());
        }
    }
}
