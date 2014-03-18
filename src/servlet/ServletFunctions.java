package servlet;

import database.Data;
import database.Information;
import database.messages.Error;
import database.objects.*;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Task;

/**
 * @author Tamino Hartmann
 *         Class for handling all the serious tasks of the servlet. Mainly there to keep Servlet comparatively clean.
 */
public class ServletFunctions {
    private static ServletFunctions INSTANCE;
    private final String TAG = "ServletFunctions";
    private Messenger log;
    private EventModuleManager moduleManager;

    private ServletFunctions() {
        log = Messenger.getInstance();
        moduleManager = EventModuleManager.getInstance();
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
                // Admin should use user_read_any!
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
                if (sentUser.isAdmin()) {
                    return new Error("IllegalChange", "You do not have the rights to modify your permissions!");
                }
                // We need to catch a password change, as it must be hashed:
                String password = sentUser.getPwdHash();
                if (password != null && !password.isEmpty()) {
                    // hash:
                    sentUser.setPwdHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
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
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        // Because we'll need these two rather often:
        Data data, message;
        switch (task) {
            case USER_READ_ANY:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                data = moduleManager.handleTask(Task.User.READ, arrival.getObject());
                message = checkDataMessage(data);
                if (message == null) {
                    // todo how to strip password from all users?
                    // return ((User) data).safeClone();
                    return data;
                } else {
                    return message;
                }
            case USER_ADD:
                // TODO: Input sanitation? Check!
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                // We need to hash the password
                User tempUser = (User) arrival.getObject();
                tempUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
                return moduleManager.handleTask(Task.User.CREATE, arrival.getObject());
            case USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.UPDATE, arrival.getObject());
            case USER_DELETE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.DELETE, arrival.getObject());
            case LOCATION_READ:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.READ, arrival.getObject());
            case LOCATION_ADD:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                // TODO update area?
                return moduleManager.handleTask(Task.Location.CREATE, arrival.getObject());
            case LOCATION_REMOVE:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.DELETE, arrival.getObject());
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
            case ADMIN_READ_ALL:
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
                return message;
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
}
