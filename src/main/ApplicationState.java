package main;

/**
 * Represents the state of the Chatter application.
 * Crucial class in the fluid and cohesive functioning of main().
 */
public class ApplicationState {
    private AppStateValue stateValue;

    /**
     * constructor for ApplicationState.
     */
    public ApplicationState() {
        stateValue = AppStateValue.LOGIN_PANEL; //default starting state value. (first screen on app startup)
    }

    /**
     * getter for the application state.
     * @return app state value.
     */
    public AppStateValue getAppState() { return stateValue; }

    /**
     * setter for application state value.
     * @param state value which shall correspond to the app state.
     */
    public void setAppState(AppStateValue state) {
        stateValue = state;
    }
}
