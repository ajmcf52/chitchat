package main;

/**
 * this enum represents the possible state values
 * that can be taken on within ApplicationState.
 */
public enum AppStateValue {
    LOGIN_PANEL, // when the app shows the login screen.
    CHOICE_PANEL, // when the app shows the choices "Join Room", "Create a Room", or "Back" (to be implemented).
    CHATTING, //signifies when a user has created and/or joined a chat room.
    ROOM_SELECT // at the room selection screen
}
