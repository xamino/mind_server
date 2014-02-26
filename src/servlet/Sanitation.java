package servlet;

import database.Data;
import database.objects.Arrival;
import database.objects.Error;
import database.objects.Message;
import database.objects.Task;
import logger.Messenger;

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
     * TAG for log.
     */
    private final String TAG = "Sanitation";
    /**
     * HashMap containing the sessions and their last access time.
     */
    private HashMap<String, Long> sessions;
    /**
     * SecureRandom for generating the hashes.
     */
    private SecureRandom random;
    /**
     * Messenger as log.
     */
    private Messenger log;

    /**
     * Private constructor. Use getInstance() to get an object reference.
     */
    private Sanitation() {
        sessions = new HashMap<>();
        random = new SecureRandom();
        log = Messenger.getInstance();
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
            // This means something went wrong. Badly.
            answer = new database.objects.Error("Illegal POST", "POST does not conform to API! Check that all keys are valid and the values set!");
            return answer;
        } else if (!checkSession(arrival.getSessionHash())) {
            // If no session exists, the reply is either illegal or a login / registration.
            String taskString = arrival.getTask();
            Task.Server task = Task.Server.safeValueOf(taskString);
            switch (task) {
                case REGISTRATION:
                    answer = registration(arrival);
                    break;
                case LOGIN:
                    answer = login(arrival);
                    break;
                default:
                    // Called when enum is ERROR!
                    log.log(TAG, "Unknown task!");
                    answer = new Error("POST unauthenticated TASK FAIL", "Unknown unauthenticated task, illegal! Should you be logged in?");
            }
            return answer;
        }
        return null;
    }

    /**
     * Login method.
     *
     * @param arrival The Arrival object to use.
     * @return Return message.
     */
    private Data login(Arrival arrival) {
        log.log(TAG, "UNKNOWN USER logged in!");
        return new Message(createSession());
    }

    /**
     * Registration method.
     *
     * @param arrival The Arrival object to use.
     * @return Return message.
     */
    private Data registration(Arrival arrival) {
        log.log(TAG, "REGISTRATION");
        return new Message("Registration doesn't yet work, just use LOGIN, you'll receive a valid session.");
    }

    /**
     * Function that hashes a password using a salt via BCrypt.
     *
     * @param password The password to salt and hash.
     * @return The hashed password.
     */
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
}