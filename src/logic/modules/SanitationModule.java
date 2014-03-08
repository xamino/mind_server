package logic.modules;

import database.Data;
import database.messages.Error;
import database.messages.Success;
import database.objects.User;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Module;
import logic.Task;
import servlet.BCrypt;
import servlet.Servlet;
import servlet.Servlet.Arrival;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

//todo: – on a session timeout the session is only removed if it is used for a query again. Need to remove sessions
//        even if they silently timeout!

/**
 * @author Tamino Hartmann
 *         Class that handles the user sessions.
 */
public class SanitationModule extends Module {

    /**
     * Session timeout in milliseconds. Cannot be changed during runtime!
     */
    private final long TIMEOUT = 15 * 60 * 1000;
    /**
     * TAG for log.
     */
    private final String TAG = "SanitationModule";
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
     * Constructor. Creates the hashmap, the secure random instance, and gets an instance of the log.
     */
    public SanitationModule() {
        sessions = new HashMap<>();
        random = new SecureRandom();
        log = Messenger.getInstance();
    }

    /**
     * Main task multiplexing method for sanitation. Handles data validation and calls the correct methods for the given
     * task.
     *
     * @param task    The @{Task.SanitationModule} to do.
     * @param request The accompanying data. Must be Arrival, otherwise an error will be returned.
     * @return A message containing the status of the request.
     */
    @Override
    public Data run(Task task, Data request) {
        if (!(task instanceof Task.Sanitation)) {
            // Check that it is one of our tasks...
            return new Error("WrongTaskType", "SanitationModule was called with the wrong task type!");
        }
        if (!(request instanceof Arrival)) {
            // SanitationModule ALWAYS only uses and expects Arrival!
            return new Error("WrongObjectType", "SanitationModule was called with the wrong object type!");
        }
        Arrival arrival = (Arrival) request;
        // As many operations require it, we pull the User object out now. Note that it might be NULL depending on the
        // task! As this might be legal, we do not throw an immediate error.
        User user = null;
        if (arrival.getObject() instanceof User) {
            user = (User) arrival.getObject();
        }
        final Task.Sanitation todo = (Task.Sanitation) task;
        switch (todo) {
            case LOGIN:
                if (user != null) {
                    return login(user);
                }
                break;
            case LOGOUT:
                destroySession(arrival.getSessionHash());
                return new Success("LoggedOut", "You have been successfully logged out.");
            // break;
            case REGISTRATION:
                if (user != null) {
                    return registration(user);
                }
                break;
            case CHECK:
                if (checkSession(arrival.getSessionHash())) {
                    return new Success("ValidSession", "The session is valid.");
                } else {
                    return new Error("SessionInvalid", "The session is NOT valid!");
                }
                // break;
            default:
                return new Error("UnknownTask", "Unknown task sent to SanitationModule!");
        }
        return new Error("SanitationTaskError", "A task failed to complete – have you supplied the correct object type?");
    }

    /**
     * Checks if a session is still valid. This is based on the hash being in the sessions HashMap and the last access
     * to the session not being older then the timeout. If the session is valid, the session last access time is
     * refreshed, resetting the timeout in effect.
     *
     * @param sessionHash The session hash used to identify the session.
     * @return True if session is valid, false else.
     */
    private boolean checkSession(String sessionHash) {
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
    private String createSession() {
        String sessionHash = new BigInteger(130, random).toString(32);
        sessions.put(sessionHash, System.currentTimeMillis());
        return sessionHash;
    }

    /**
     * Destroys a session, removing it from the system.
     *
     * @param sessionHash The identifying session hash that will be removed.
     */
    private void destroySession(String sessionHash) {
        if (sessions.containsKey(sessionHash)) {
            sessions.remove(sessionHash);
        }
    }

    /**
     * Login method.
     *
     * @return Return message.
     */
    private Data login(User user) {
        /*
        // TAKE OUT; ONLY FOR DEBUG
        if (user == null) {
            return new Success("LOGIN", createSession());
        }
        */
        Data object = EventModuleManager.getInstance().handleTask(Task.User.READ, user);
        Data answer = Servlet.checkDataMessage(object);
        if (answer != null) {
            // this means 99% of the time that a user wasn't found.
            // To avoid allowing to find usernames with this method, we return the same message as if the login
            // simply used the wrong information.
            log.log(TAG, "Unregistered user tried login!");
            return new Error("LoginFailed", "Wrong user email or wrong password.");
        }
        User check = (User) object;
        if (BCrypt.checkpw(user.getPwdHash(), check.getPwdHash())) {
            return new Success("Login", createSession());
        } else {
            return new Error("LoginFailed", "Wrong user email or wrong password.");
        }
    }

    /**
     * Registration method.
     *
     * @param user The user object to register.
     * @return Return message.
     */
    private Data registration(User user) {
        user.setPwdHash(BCrypt.hashpw(user.getPwdHash(), BCrypt.gensalt(12)));
        Data msg = EventModuleManager.getInstance().handleTask(Task.User.CREATE, user);
        if (msg instanceof Success) {
            return new Success("Registered", "Registered \"" + user.getEmail() + "\".");
        } else {
            log.error(TAG, "Error creating a user!");
            return msg;
        }
    }
}