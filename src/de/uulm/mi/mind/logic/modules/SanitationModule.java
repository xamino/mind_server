package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.objects.Authenticated;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Message;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.logic.Task;
import de.uulm.mi.mind.servlet.BCrypt;
import de.uulm.mi.mind.servlet.Servlet.Arrival;
import de.uulm.mi.mind.servlet.ServletFunctions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
                // todo why not return an error?
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
                if (checkSession(arrival.getSessionHash())) {
                    // If valid, return the corresponding user object. Fresh from the DB to keep in sync with updates
                    // To do that, we first need to get the new object:
                    ActiveUser activeUser = sessions.get(arrival.getSessionHash());
                    Data object = readAuthFromDB(activeUser.user);
                    Data msg = ServletFunctions.getInstance().checkDataMessage(object);
                    if (msg != null) {
                        log.error(TAG, "WARNING: Check failed because no user or error was returned from DB!");
                    } else if (object instanceof Authenticated) {
                        // If everything is okay, we return the current user and leave this method
                        Authenticated currentUser = ((Authenticated) object);
                        activeUser = new ActiveUser(currentUser, System.currentTimeMillis(), arrival.getSessionHash());
                        sessions.put(arrival.getSessionHash(), activeUser);
                        return (Data) currentUser;
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
            log.log(TAG, "User " + activeUser.user.readIdentification() + " has logged out.");
        }
    }

    /**
     * Login method. If valid, stores the logged in user in the session map with the current time.
     *
     * @return Return message.
     */
    private Data login(Authenticated user) {
        Data object = readAuthFromDB(user);
        // Check if message:
        Data answer = ServletFunctions.getInstance().checkDataMessage(object);
        if (answer != null) {
            // this means 99% of the time that a user wasn't found.
            // To avoid allowing to find usernames with this method, we return the same message as if the login
            // simply used the wrong information.
            log.log(TAG, "Unregistered user tried login!");
            return new Error("LoginFailed", "Wrong user email or wrong password.");
        }
        // This means we have a valid user object:
        Authenticated auth = (Authenticated) object;
        // Now to check the password
        if (BCrypt.checkpw(user.readAuthentication(), auth.readAuthentication())) {
            // Everything okay, so create a hash:
            String sessionHash = new BigInteger(130, random).toString(32);
            // Set to false as default so PD don't trigger last access time
            boolean dateIsNull = false;
            // If user, change last access time
            if (user instanceof User) {
                User check = ((User) auth);
                // Set last access time
                dateIsNull = (check.getLastAccess() == null);
                check.setLastAccess(new Date());
            }
            // Create the activeUser object using the correct, database user:
            ActiveUser activeUser = new ActiveUser(auth, System.currentTimeMillis(), sessionHash);
            // Save the session:
            sessions.put(sessionHash, activeUser);
            log.log(TAG, "User " + auth.readIdentification() + " has logged in.");
            // If the date was null, the user has never logged in before, so we signal that to the client
            if (dateIsNull) {
                return new Message("FirstLogin", sessionHash);
            } else {
                // Return the hash for future references:
                return new Success("Login", sessionHash);
            }
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
        // hash pwd
        user.setPwdHash(BCrypt.hashpw(user.getPwdHash(), BCrypt.gensalt(12)));
        // set access time
        user.setLastAccess(new Date());
        Data msg = EventModuleManager.getInstance().handleTask(Task.User.CREATE, user);
        if (msg instanceof Success) {
            log.log(TAG, "User " + user.readIdentification() + " has been registered.");
            return new Success("Registered", "Registered to '" + user.getEmail() + "'.");
        } else {
            log.error(TAG, "Error creating a user!");
            return msg;
        }
    }

    /**
     * Helper method that reads the correct object from the DB depending on its super type.
     *
     * @param incomplete The authenticated object which to read.
     * @return User, PublicDisplay, or Information.
     */
    private Data readAuthFromDB(final Authenticated incomplete) {
        Data tempData;
        if (incomplete instanceof User) {
            // Try reading the user from the database
            tempData = EventModuleManager.getInstance().handleTask(Task.User.READ, new User(null, incomplete.readIdentification()));
            if (tempData instanceof DataList && ((DataList) tempData).size() == 1) {
                return (Data) ((DataList) tempData).get(0);
            }
        } else if (incomplete instanceof PublicDisplay) {
            tempData = EventModuleManager.getInstance().handleTask(Task.Display.READ, new PublicDisplay(incomplete.readIdentification(), null, null, 0, 0));
            if (tempData instanceof DataList && ((DataList) tempData).size() == 1) {
                return (Data) ((DataList) tempData).get(0);
            }
        }
        // If we reach this, we've found an error
        return new Error("UnknownAuthenticatedType", "Unknown user object tried login!");
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