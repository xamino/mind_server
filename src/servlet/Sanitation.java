package servlet;

import database.Data;
import database.objects.Arrival;
import database.objects.Error;
import database.objects.Message;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

//todo: – on a session timeout the session is only removed if it is used for a query again. Need to remove sessions
//        even if they silently timeout!

/**
 * @author Tamino Hartmann
 *         Class that handles the user sessions.
 */
public class Sanitation {

    /**
     * Instance of this class. Access via getInstance().
     */
    private static Sanitation INSTANCE;
    /**
     * Session timeout in milliseconds. Cannot be changed during runtime!
     */
    private final long TIMEOUT = 15 * 60 * 1000;
    /**
     * HashMap containing the sessions and their last access time.
     */
    private HashMap<String, Long> sessions;
    /**
     * SecureRandom for generating the hashes.
     */
    private SecureRandom random;

    /**
     * Private constructor. Use getInstance() to get an object reference.
     */
    private Sanitation() {
        sessions = new HashMap<>();
        random = new SecureRandom();
    }

    /**
     * Method returns a reference of this class.
     *
     * @return The reference to a created object.
     */
    public static Sanitation getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Sanitation();
        }
        return INSTANCE;
    }

    /**
     * Checks if a session is still valid. This is based on the hash being in the sessions HashMap and the last access
     * to the session not being older then the timeout. If the session is valid, the session last access time is
     * refreshed, resetting the timeout in effect.
     *
     * @param sessionHash The session hash used to identify the session.
     * @return True if session is valid, false else.
     */
    public boolean checkSession(String sessionHash) {
        if (sessions.containsKey(sessionHash)) {
            // check for timeout
            long timeDelta = System.currentTimeMillis() - sessions.get(sessionHash);
            if (timeDelta > TIMEOUT) {
                // this means the session has expired
                // remove, as expired:
                this.destroySession(sessionHash);
                return false;
            }
            // update time if session is valid, resetting the timeout:
            sessions.put(sessionHash, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    /**
     * Creates a session, saving it with the current time in the HashMap.
     *
     * @return The session hash – required for future authentication, so save it!
     */
    public String createSession() {
        String sessionHash = new BigInteger(130, random).toString(32);
        sessions.put(sessionHash, System.currentTimeMillis());
        return sessionHash;
    }

    /**
     * Destroys a session, removing it from the system.
     *
     * @param sessionHash The identifying session hash that will be removed.
     */
    public void destroySession(String sessionHash) {
        if (sessions.containsKey(sessionHash)) {
            sessions.remove(sessionHash);
        }
    }

    /**
     * Method that checks whether an arrival is valid and not null, then handles login if applicable and checks the session.
     *
     * @param arrival The Arrival object to check.
     * @return Either an Error or a Message if something is wrong; if everything checks out, then null.
     */
    public Data checkArrival(Arrival arrival) {
        Data answer;
        if (arrival == null || !arrival.isValid()) {
            answer = new database.objects.Error("Illegal POST", "POST does not conform to API! Check that all keys are valid and the values set!");
            return answer;
        } else if (!checkSession(arrival.getSessionHash())) {
            // login only available when logged out
            if (arrival.getTask().equals("login")) {
                answer = new Message(createSession());
            } else {
                answer = new Error("POST Authentication FAIL", "You seem not to be logged in!");
            }
            return answer;
        }
        return null;
    }
}