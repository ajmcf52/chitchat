package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import main.Constants;

/**
 * This class is responsible for performing many of the miscellaneous tasks required for the Chatter application to work.
 * 
 * registry functions:
 * I) store user count, providing fresh user ID numbers (UIDs) for new users.
 */
public class Registry {
    private static volatile int userCount = 0;
    private static Object userCountLock = new Object();
    private static boolean running = false;

    public static void main(String[] args) {
        try {
            System.out.println("UCL --> " + userCountLock.toString());
            Socket socket; // socket variable for accepted connections.
            ServerSocket serverSocket = new ServerSocket(Constants.REGISTRY_PORT); // the server socket; accepts connections.

            System.out.println("Server Registry listening on port" + Constants.REGISTRY_PORT);
            running = true;
            while (running) {
                socket = serverSocket.accept();
                System.out.println("Connection received by Registry");
                RequestHandler handler = new RequestHandler(socket);
                handler.start();
                System.out.println("Connection passed off to RequestHandler");
            }
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            
        } catch (IOException e) {
            System.out.println("Registry IO Error!! --> " + e.getMessage());
        }
    }

    /**
     * the purpose of this thread-based class is to be instantiated whenever the Registry receives a new incoming connection.
     * the resulting Socket for that connection is passed off to this RequestHandler.
     * 
     * The nature of the desired request (determined by the first message received via the Socket) will determine the 
     * RequestHandler's course of action.
     * 
     * I) For NewUserRequests, the current course of action is to send back a UID for said user. 
     * Down the line, the Registry will keep static storage of all users logged in to Chatter.
     */
    private static class RequestHandler extends Thread {
        private Socket socket;

        /**
         * constructor.
         * @param sock - connection socket to be used for servicing a request.
         */
        public RequestHandler(Socket sock) {
            socket = sock;
        }

        public void run() {
            // handle the request!
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                String requestString = in.readLine();

                switch (requestString) {
                    /*
                     * here, we are servicing a NewUserRequest. To fulfill this request at a basic level,
                     * all we have to do is send back a fresh UID for the user, incrementing userCount after doing so.
                     */
                    case (Constants.NEW_USER_REQ): {
                        String alias = in.readLine(); // ChatUser's alias. This can be saved in a HashMap at a later date.
                        int uidNum = -1;

                        // get a hold of userCount (synchronized access)
                        synchronized (userCountLock) {
                            // increment userCount, copy value into a placeholder, and exit sync block
                            userCount++;
                            uidNum = userCount;
                        }

                        String uid = Constants.UID_PREFIX + String.valueOf(uidNum); // generate UID string

                        // whisk the UID back over the socket.
                        out.write(uid);
                        // Work done! Prepare for exit.
                        out.flush();
                        out.close();
                        break;
                    }
                    default: {
                        System.out.println("RequestHandler -> default case.");
                    }
                }

            } catch (IOException e) {
                System.out.println("RequestHandler IO Error! -->" + e.getMessage());
            }
        }
    }
}