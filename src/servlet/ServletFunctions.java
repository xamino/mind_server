package servlet;

import database.Data;
import database.Information;
import database.messages.Error;
import database.messages.Message;
import database.messages.Success;
import database.objects.*;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Task;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author Tamino Hartmann
 *         Class for handling all the serious tasks of the servlet. Mainly there to keep Servlet comparatively clean.
 */
public class ServletFunctions {
    private static ServletFunctions INSTANCE;
    private final String TAG = "ServletFunctions";
    private Messenger log;
    private EventModuleManager moduleManager;
    private SecureRandom random;

    private ServletFunctions() {
        log = Messenger.getInstance();
        moduleManager = EventModuleManager.getInstance();
        random = new SecureRandom();
    }

    public static ServletFunctions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServletFunctions();
        }
        return INSTANCE;
    }

    /**
     * Handles all generally accessible tasks that only require a valid user.
     *
     * @param arrival
     * @return
     */
    public Data handleNormalTask(Servlet.Arrival arrival, User user) {
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        switch (task) {
            case ECHO:
                // Simple echo test for checking if the server can parse the data
                return arrival.getObject();
            case USER_READ:
                // WARNING: do NOT allow ANY USER TO BE READ HERE – SECURITY LEAK!
                // Admin should use admin_user_read!
                return user.safeClone();
            case USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new database.messages.Error("WrongObject", "You supplied a wrong object for this task!");
                }
                User sentUser = (User) arrival.getObject();
                // Email is primary key, thus can not be changed!
                if (!sentUser.getEmail().equals(user.getEmail())) {
                    return new Error("IllegalChange", "Email can not be changed in an existing user");
                }
                // Make sure that you can't set yourself to admin
                if (sentUser.isAdmin() && !user.isAdmin()) {
                    return new Error("IllegalChange", "You do not have the rights to modify your permissions!");
                }
                // Also make sure you keep your admin status
                sentUser.setAdmin(user.isAdmin());
                // keep last access time
                sentUser.setLastAccess(user.getLastAccess());
                // We need to catch a password change, as it must be hashed:
                String password = sentUser.getPwdHash();
                if (password != null && !password.isEmpty()) {
                    // hash:
                    sentUser.setPwdHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
                } else {
                    sentUser.setPwdHash(user.getPwdHash());
                }
                // This means that the name isn't to be changed:
                if (sentUser.getName() == null) {
                    sentUser.setName(user.getName());
                }
                // Note that the session user object now needs to be updated. This is done the next time the user
                // sends a request through SanitationModule; it will always get the up to date object from the
                // database.
                return moduleManager.handleTask(Task.User.UPDATE, sentUser);
            case USER_DELETE:
                // To catch some errors, we enforce that no object has been passed along:
                if (arrival.getObject() != null) {
                    // If null...
                    return new Error("IllegalUserRemove", "To delete your user, you must not pass any object along.");
                }
                // Remove session
                moduleManager.handleTask(Task.Sanitation.LOGOUT, user);
                // Otherwise ignore all else, just delete:
                return moduleManager.handleTask(Task.User.DELETE, user);
            case POSITION_FIND:
                // TODO position returns an error, do we even need to check here? Check with Andy!
                return moduleManager.handleTask(Task.Position.FIND, arrival.getObject());
            case TOGGLE_ADMIN:
                // TODO remove this, only for test!
                user.setAdmin(!user.isAdmin());
                return moduleManager.handleTask(Task.User.UPDATE, user);
            default:
                return null;
        }
    }

    /**
     * Handles tasks that are specifically only for the admin class user.
     *
     * @param arrival
     * @return
     */
    public Data handleAdminTask(Servlet.Arrival arrival, User user) {
        // Better safe than sorry:
        if (!user.isAdmin()) {
            log.error(TAG, "User " + user.readIdentification() + " almost accessed admin functions!");
            return new Error("IllegalAccess", "You do not have permission for this task.");
        }
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        // Because we'll need these two rather often:
        Data data, message;
        User tempUser;
        PublicDisplay display;
        switch (task) {
            // USERS -----------------------------------------------------------------------------
            case ADMIN_USER_READ:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                data = moduleManager.handleTask(Task.User.READ, arrival.getObject());
                message = checkDataMessage(data);
                if (message == null) {
                    // todo how to strip password from all users?
                    // return ((User) data).safeClone();
                    return data;
                }
                return nullMessageCatch(message);
            case ADMIN_USER_ADD:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                tempUser = (User) arrival.getObject();
                if (tempUser.getEmail() != null && !tempUser.getEmail().isEmpty()) {
                    if (tempUser.getPwdHash() != null && !tempUser.getPwdHash().isEmpty()) {
                        String newPassword = tempUser.getPwdHash();
                        tempUser.setPwdHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
                        return moduleManager.handleTask(Task.User.CREATE, tempUser);
                    } else {
                        // This means that we generate a password
                        String key = new BigInteger(130, random).toString(32);
                        // hash it
                        tempUser.setPwdHash(BCrypt.hashpw(key, BCrypt.gensalt(12)));
                        data = moduleManager.handleTask(Task.User.CREATE, tempUser);
                        // If the operation was a success, we need to send the generated key back
                        if (data instanceof Success) {
                            data = new Message("UserAddSuccessKey", key);
                        }
                        return data;
                    }
                }
                return new Error("IllegalAdd", "Email is primary key! May not be empty.");
            case ADMIN_USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                tempUser = (User) arrival.getObject();
                if (tempUser.getEmail() == null || tempUser.getEmail().isEmpty()) {
                    return new Error("IllegalChange", "Email must not be empty!");
                }
                data = moduleManager.handleTask(Task.User.READ, new User(null, tempUser.getEmail()));
                message = checkDataMessage(data);
                if (message == null && data instanceof DataList && ((DataList) data).size()==1) {
                    User originalUser = (User) ((DataList) data).get(0);
                    tempUser.setEmail(originalUser.getEmail());
                    tempUser.setLastAccess(originalUser.getLastAccess());
                    if (tempUser.getPwdHash() != null && !tempUser.getPwdHash().isEmpty()) {
                        tempUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
                    } else {
                        tempUser.setPwdHash(originalUser.getPwdHash());
                    }
                    // This means the name isn't to be changed:
                    if (tempUser.getName() == null) {
                        tempUser.setName(originalUser.getName());
                    }
                    return moduleManager.handleTask(Task.User.UPDATE, tempUser);
                }
                return nullMessageCatch(message);
            case ADMIN_USER_DELETE:
                log.log("DELETE", arrival.getObject().toString());
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.DELETE, arrival.getObject());
            // LOCATION --------------------------------------------------------------------------
            // TODO sanitize and make sane!
            case LOCATION_READ:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.READ, arrival.getObject());
            case LOCATION_ADD:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.CREATE, arrival.getObject());
            case LOCATION_REMOVE:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.DELETE, arrival.getObject());
            // AREAS -----------------------------------------------------------------------------
            // TODO sanitize and make sane!
            case AREA_READ:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.READ, arrival.getObject());
            case AREA_ADD:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.CREATE, arrival.getObject());
            case AREA_UPDATE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.UPDATE, arrival.getObject());
            case AREA_REMOVE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.DELETE, arrival.getObject());
            // DISPLAYS ---------------------------------------------------------------------------
            case DISPLAY_READ:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Display.READ, arrival.getObject());
            case DISPLAY_ADD:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                display = (PublicDisplay) arrival.getObject();
                if (display.getIdentification() != null && !display.getIdentification().isEmpty()) {
                    if (display.getToken() != null && !display.getToken().isEmpty()) {
                        display.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
                        return moduleManager.handleTask(Task.Display.CREATE, arrival.getObject());
                    } else {
                        // This means that we generate a password
                        String key = new BigInteger(130, random).toString(32);
                        // hash it
                        display.setToken(BCrypt.hashpw(key, BCrypt.gensalt(12)));
                        data = moduleManager.handleTask(Task.Display.CREATE, display);
                        // If the operation was a success, we need to send the generated key back
                        if (data instanceof Success) {
                            data = new Message("DisplayAddSuccessKey", key);
                        }
                        return data;
                    }
                }
                return new Error("IllegalAdd", "Identification is primary key! May not be empty.");
            case DISPLAY_UPDATE:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                // id and token require special consideration
                display = (PublicDisplay) arrival.getObject();
                if (display.getIdentification() == null || display.getIdentification().isEmpty()) {
                    return new Error("IllegalChange", "Identification must not be empty!");
                }
                data = moduleManager.handleTask(Task.Display.READ, new PublicDisplay(display.getIdentification(), null, null, 0, 0));
                message = checkDataMessage(data);
                if (message == null && data instanceof DataList) {
                    if (((DataList) data).size()!= 1) {
                        return new Error("DisplayUpdateError","Display was not found!");
                    }
                    PublicDisplay originalDisplay = (PublicDisplay) ((DataList) data).get(0);
                    display.setIdentification(originalDisplay.getIdentification());
                    if (display.getToken() != null && !display.getToken().isEmpty()) {
                        display.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
                    } else {
                        display.setToken(originalDisplay.getToken());
                    }
                    return moduleManager.handleTask(Task.Display.UPDATE, display);
                }
                return nullMessageCatch(message);
            case DISPLAY_REMOVE:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Display.DELETE, arrival.getObject());
            // Special admin stuff ---------------------------------------------------------------
            case READ_ALL_ADMIN:
                // TODO: can now be drastly improved with new filter, change!
                data = moduleManager.handleTask(Task.User.READ, null);
                message = checkDataMessage(data);
                if (message == null && data instanceof DataList) {
                    DataList list = (DataList) data;
                    DataList<User> admins = new DataList<User>();
                    for (Object us : list) {
                        if (!(us instanceof User))
                            continue;
                        if (((User) us).isAdmin())
                            admins.add((User) us);
                    }
                    return admins;
                }
                return nullMessageCatch(message);
            case ADMIN_ANNIHILATE_AREA:
                log.log(TAG, "Removing all area objects!");
                return moduleManager.handleTask(Task.Area.ANNIHILATE, null);
            case ADMIN_ANNIHILATE_USER:
                log.log(TAG, "Removing all users!");
                return moduleManager.handleTask(Task.User.ANNIHILATE, null);
            default:
                return null;
        }
    }

    // TODO
    public Data handleDisplayTask(Servlet.Arrival arrival, PublicDisplay display) {
        return new Error("Unimplemented", "No functions for this have been implemented so far!");
    }

    /**
     * Method that checkes if data returned from the modules is a message, in which case it is returned, or null, in
     * which case an error message is returned. If the data is anything else, it is considered to be a valid reply and
     * null is returned.
     *
     * @param data The data to check.
     * @return An Information object if data is such, else null.
     */
    public Information checkDataMessage(Data data) {
        if (data == null) {
            return new Error("DATA NULL", "Data requested returned NULL, should NOT HAPPEN!");
        } else if (data instanceof Information) {
            return (Information) data;
        } else {
            // This means that answer is manually set.
            return null;
        }
    }

    /**
     * Safety catch for messages. Should prevent receiving illegal task messages when the task was legal but no
     * message was written.
     *
     * @param msg The message variable to check for null.
     * @return A message. Guaranteed!
     */
    private Information nullMessageCatch(Data msg) {
        if (msg == null || !(msg instanceof Information)) {
            log.error(TAG, "NullMessage happened! Shouldn't happen, so fix!");
            return new Error("NullMessage", "Something went wrong in the task but no message was written!");
        }
        return (Information) msg;
    }
}
