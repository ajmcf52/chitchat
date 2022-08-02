package worker;

import java.net.Socket;

import com.apple.eawt.Application;

import misc.Constants;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.ChatUser;
import main.AppStateValue;
import main.ApplicationState;

/**
 * This thread-based class is responsible for communicating with the
 * Registry to get a new chat room set up for a given ChatUser, as
 * denoted by the user alias.
 * 
 */
public class RoomSetupWorker extends Thread {
    
    private ChatUser chatUser; // ChatUser we are setting up as the intended host.
    private Object chatUserLock; // ChatUser will wait to be notified on this (signifies work has been done)
    private ApplicationState appState; // modified to let main() know where we are at.

    /**
     * constructor.
     * @param ali user alias
     * @param chatLock notified to alert ChatUser in main() of progress.
     * @param state for main() loop control.
     */
    public RoomSetupWorker(ChatUser chUser, Object chatLock, ApplicationState state) {
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
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader((socket.getInputStream())));

            // write the RoomSetupRequest to the Registry!
            // New room request signifier first, followed immediately by the intended chat user host alias.
            String roomSetupMsg = Constants.NEW_ROOM_REQ + '\n' + chatUser.getAlias() + '\n';
            out.write(roomSetupMsg);
            out.flush();

            /* Registry should respond with the inet address of the SessionThread's server socket,
            which the ChatUser will connect to. Once connected to the SessionThread, this will open
            its channel of communication with that chat session.
            */
            String seshConnectionInfo = in.readLine();
            String[] IpAndPort = seshConnectionInfo.split(":");
            String seshIp = IpAndPort[0];
            int seshPortNum = -1;
            try {
                seshPortNum = Integer.valueOf(IpAndPort[1]);
            } catch (Exception e) {
                System.out.println("Error in RSW retrieving port number from Registry --> " + e.getMessage());
            }
            
            chatUser.initializeSessionInfo(seshIp, seshPortNum);

            // work is done! prepare for exit, and modify app state accordingly.
            in.close();
            out.close();
            socket.close();
            appState.setAppState(AppStateValue.CHATTING);
            
            // notify ChatUser that the work has been done.
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }

        } catch (Exception e) {
            System.out.println("RoomSetupThread Exception --> " + e.getMessage());
        }
    }
}
