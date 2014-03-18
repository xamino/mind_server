package logic.modules;

import database.Authenticated;
import database.Data;
import database.messages.Error;
import database.messages.Success;
import database.objects.User;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Module;
import logic.Task;
import servlet.BCrypt;
import servlet.Servlet.Arrival;
import servlet.ServletFunctions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

//todo: – on a session timeout the session is only removed if it is used for a query again. Need to remove sessions
//        even if they silently timeout!

/**
 * @author Tamino Hartmann
 *         Class that handles the user sessions. Note that this includes the PublicDisplays, but ONLY for CHECK.
 *         Otherwise the public displays must be managed by the admin or users.
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
    private HashMap<String, ActiveUser> sessions;
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
        // Pull out authenticated
        Authenticated user = null;
        if (arrival.getObject() instanceof Authenticated) {
            user = (Authenticated) arrival.getObject();
        }
        // Correctly cast
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
                // Registration REQUIRES a User object!
                if (user != null && user instanceof User) {
                    if (user.readIdentification().isEmpty() || user.readAuthentication().isEmpty()) {
                        return new Error("IllegalRegistrationValues", "Email and password may not be empty!");
                    }
                    return registration((User) user);
                }
                break;
            case CHECK:
                // TODO make this work for Authenticated
                if (checkSession(arrival.getSessionHash())) {
                    // If valid, return the corresponding user object. Fresh from the DB to keep in sync with updates
                    // To do that, we first need to get the new object:
                    User filter = new User("", sessions.get(arrival.getSessionHash()).user.readIdentification());
                    Data update = EventModuleManager.getInstance().handleTask(Task.User.READ, filter);
                    if (update instanceof User) {
                        // If everything is okay, we return the current user and leave this method
                        User currentUser = (User) update;
                        // We could check whether we even need to update the user object, but we need to reset the timer anyway
                        ActiveUser activeUser = new ActiveUser(currentUser, System.currentTimeMillis(), arrival.getSessionHash());
                        sessions.put(arrival.getSessionHash(), activeUser);
                        return currentUser;
                    } else if (!(update instanceof Error)) {
                        // DB returns an error if no user was found, in which case this warning is not required
                        log.error(TAG, "WARNING: Check failed because no user or error was returned from DB!");
                    }
                    // If we haven't returned, something went wrong
                    // destroy session to be safe
                    destroySession(arrival.getSessionHash());
                    return new Error("SessionCheckError", "User couldn't be found in DB! You have been logged out.");
                } else {
                    return new Error("SessionInvalid", "The session is NOT valid!");
                }
                // break;
            default:
                log.error(TAG, "Unknown task #" + todo + "# sent to SanitationModule! Shouldn't happen!");
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
            // get ActiveUser object
            ActiveUser user = sessions.get(sessionHash);
            // check for timeout
            long timeDelta = System.currentTimeMillis() - user.timestamp;
            if (timeDelta > TIMEOUT) {
                // this means the session has expired
                // remove, as expired:
                this.destroySession(sessionHash);
                return false;
            }
            // update time if session is valid, resetting the timeout:
            user.timestamp = System.currentTimeMillis();
            sessions.put(sessionHash, user);
            return true;
        }
        return false;
    }

    /**
     * Destroys a session, removing it from the system. Will also log out all other sessions of the user!
     *
     * @param sessionHash The identifying session hash that will be removed.
     */
    private void destroySession(String sessionHash) {
        if (sessions.containsKey(sessionHash)) {
            ActiveUser activeUser = sessions.get(sessionHash);
            // remove primary session
            sessions.remove(sessionHash);
            // remove all other sessions with the same user
            Collection<ActiveUser> values = sessions.values();
            ArrayList<String> hashes = new ArrayList<>();
            for (ActiveUser check : values) {
                if (check.user.readIdentification().equals(activeUser.user.readIdentification())) {
                    hashes.add(check.hash);
                }
            }
            for (String hash : hashes) {
                sessions.remove(hash);
            }
        }
    }

    /**
     * Login method. If valid, stores the logged in user in the session map with the current time.
     *
     * @return Return message.
     */
    // TODO make it work for Authenticated
    private Data login(Authenticated user) {
        // Try reading the user from the database
        Data object = EventModuleManager.getInstance().handleTask(Task.User.READ, new User(null, user.readIdentification()));
        // Check if message:
        Data answer = ServletFunctions.getInstance().checkDataMessage(object);
        // TODO here -->
        if (answer != null || !(object instanceof User)) {
            // this means 99% of the time that a user wasn't found.
            // To avoid allowing to find usernames with this method, we return the same message as if the login
            // simply used the wrong information.
            log.log(TAG, "Unregistered user tried login!");
            return new Error("LoginFailed", "Wrong user email or wrong password.");
        }
        // This means we have a valid user object:
        User check = (User) object;
        // Now to check the password
        if (BCrypt.checkpw(user.readAuthentication(), check.getPwdHash())) {
            // Everything okay, so create a hash:
            String sessionHash = new BigInteger(130, random).toString(32);
            // Create the activeUser object using the correct, database user:
            ActiveUser activeUser = new ActiveUser(check, System.currentTimeMillis(), sessionHash);
            // Save the session:
            sessions.put(sessionHash, activeUser);
            // Return the hash for future references:
            return new Success("Login", sessionHash);
        } else {
            return new Error("LoginFailed", "Wrong user email or wrong password.");
        }
    }

    /**
     * Registration method. Only works for users!
     *
     * @param user The user object to register.
     * @return Return message.
     */
    private Data registration(User user) {
        user.setPwdHash(BCrypt.hashpw(user.getPwdHash(), BCrypt.gensalt(12)));
        Data msg = EventModuleManager.getInstance().handleTask(Task.User.CREATE, user);
        if (msg instanceof Success) {
            return new Success("Registered", "Registered to '" + user.getEmail() + "'.");
        } else {
            log.error(TAG, "Error creating a user!");
            return msg;
        }
    }

    /**
     * Small data class for storing active users with the corresponding hash and timestamp of their last action.
     */
    private class ActiveUser {
        Authenticated user;
        long timestamp;
        String hash;

        ActiveUser(Authenticated user, long timestamp, String hash) {
            this.user = user;
            this.timestamp = timestamp;
            this.hash = hash;
        }
    }
}