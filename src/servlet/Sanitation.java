package servlet;

import java.util.HashMap;

/**
 * @author Tamino Hartmann
 */
public class Sanitation {

    private static Sanitation INSTANCE;
    private HashMap<String, Long> sessions;

    private Sanitation() {
        sessions = new HashMap<>();
    }

    public static Sanitation getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Sanitation();
        return INSTANCE;
    }

    public boolean secure(String sessionHash) {
        if (sessions.containsKey(sessionHash)) {
            return true;
        }
        return false;
    }

    public String createSession() {
        return "i'm a safe hash!";
    }
}
