package net;

import java.lang.Thread;

/**
 * this class represents a thread-based ChatUser within the Chatter application.
 */
public class ChatUser extends Thread {
    private String userID; //uniquely identifies this user.
    private String alias;

    /**
     * default constructor.
     */
    public ChatUser() {
        userID = "";
        alias = "";
    }

    /**
     * constructor that will more than likely be used most of the time.
     * @param uid - user ID
     * @param a - alias
     */
    public ChatUser(String uid, String a) {
        userID = uid;
        alias = a;
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


}
