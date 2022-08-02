package worker;

import java.net.Socket;

import misc.Constants;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.ChatUser;

/**
 * This thread-based class is responsible for communicating with the
 * Registry to get a new chat room set up for a given ChatUser, as
 * denoted by the user alias.
 * 
 */
public class RoomSetupWorker extends Thread {
    
    private ChatUser chatUser; // ChatUser we are setting up as the intended host.
    private Object chatUserLock; // ChatUser will wait to be notified on this (signifies work has been done)

    /**
     * constructor.
     * @param ali user alias
     */
    public RoomSetupWorker(ChatUser chUser, Object chatLock) {
        chatUser = chUser;
        chatUserLock = chatLock;
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
            String seshThreadInetAddress = in.readLine();
            chatUser.initializeSessionAddress(seshThreadInetAddress);

            // work is done! prepare for exit.
            in.close();
            out.close();
            socket.close();
            
            // notify ChatUser that the work has been done.
            synchronized (chatUserLock) {
                chatUserLock.notify();
            }

        } catch (Exception e) {
            System.out.println("RoomSetupThread Exception --> " + e.getMessage());
        }
    }
}
