package worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import com.apple.eawt.Application;

import misc.Constants;
import net.ChatUser;
import main.AppStateValue;
import main.ApplicationState;

/**
     * This class is responsible for setting up the ChatUser with its properties, namely its alias and UID.
     * The alias is passed in through the LoginPanel's KeyEventHandler.
     * UST fetches the UID from the registry.
     */
    public class UserSetupWorker extends Thread {
        
        private String alias;
        private ChatUser userRef;
        private Object chatUserLock;
        private ApplicationState appState;

        /**
         * constructor for the user setup worker.
         * @param ali String-based alias to be set as the chat user's screen name
         * @param cu chat user object reference
         * @param cuLock chat user lock (notify on this for crucial events)
         * @param state app state
         */
        public UserSetupWorker(String ali, ChatUser cu, Object cuLock, ApplicationState state) {
            alias = ali;
            userRef = cu;
            chatUserLock = cuLock;
            appState = state;
        }

        /**
         * the UserSetupThread's main course of action.
         */
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(Constants.REGISTRY_IP,Constants.REGISTRY_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send the protocol message on one line, then the alias on following line.
                out.write(Constants.NEW_USER_REQ + '\n');
                out.write(alias + '\n');
                out.flush();
                String response = in.readLine(); // should be a UID string for the user
                System.out.println(response);
                // initialize the ChatUser's fields.
                userRef.init(response, alias);
                // work is done! Prepare for exit, and modify app state accordingly.
                in.close();
                out.close();
                socket.close();
                appState.setAppState(AppStateValue.LOGIN_PANEL);

                //notify ChatterApp's thread of execution that ChatUser's initialization is done.
                synchronized (chatUserLock) {
                    chatUserLock.notify();
                }
            }
            catch (IOException e) {
                System.out.println("UserSetupThread error!! --> " + e.getMessage());
            }
        }
    }
