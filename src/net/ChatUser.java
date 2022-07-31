package net;

import java.lang.Thread;

/**
 * this class represents a thread-based ChatUser within the Chatter application.
 */
public class ChatUser extends Thread {
    private String userID; //uniquely identifies this user.
    private String alias; // this chat user's screen name.
    private String sessionInetAddress; // inet address of a chat session's server socket.

    /**
     * default constructor.
     */
    public ChatUser() {
        userID = "";
        alias = "";
        sessionInetAddress = "";
    }

    /**
     * constructor that will more than likely be used most of the time.
     * @param uid - user ID
     * @param a - alias
     */
    public ChatUser(String uid, String a) {
        userID = uid;
        alias = a;
        sessionInetAddress = "";
    }

    public void init(String uid, String a) {
        System.out.println("Initializing ChatUser " + userID + "...");
        userID = uid;
        alias = a;
    }

    /**
     * the ChatUser's main course of action.
     */
    public void run() {

    }

    /**
     * getter for userID.
     * @return userID
     */
    public String getUID() { return userID; }

    /**
     * setter for alias.
     * @param a - ChatUser's new alias
     */
    public void setAlias(String a) {
        if (a != null && a != "") {
            alias = a;
        }
    }

    /**
     * getter for alias.
     * @return ChatUser alias
     */
    public String getAlias() { return alias; }

    /**
     * this method is used to setup the socket information that
     * will be used to connect with a session thread for the sake
     * of entering and participating in a chat session.
     * @param seshInetAddr inet address of the SessionThread we must connect to participate in a given ChatSession.
     */
    public void initializeSessionAddress(String seshInetAddr) {
        sessionInetAddress = seshInetAddr;
    }
}
