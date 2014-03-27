package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.logic.Task;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Message;
import de.uulm.mi.mind.objects.messages.Success;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author Tamino Hartmann
 *         Class for handling all the serious tasks of the servlet. Mainly there to keep Servlet comparatively clean.
 */
public class ServletFunctions {
    private static ServletFunctions INSTANCE;
    private final String TAG = "ServletFunctions";
    /**
     * This controls the length of the generated keys when adding users or displays without a preset password.
     */
    private final int GENERATED_KEY_LENGTH = 6;
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
    public Data handleNormalTask(Arrival arrival, User user) {
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
                    return new de.uulm.mi.mind.objects.messages.Error("WrongObject", "You supplied a wrong object for this task!");
                }
                User sentUser = (User) arrival.getObject();
                // Email is primary key, thus can not be changed!
                if (!sentUser.getEmail().equals(user.getEmail())) {
                    return new Error("IllegalChange", "Email can not be changed in an existing user");
                }
                // Make sure that you can't set or unset yourself to admin
                if (sentUser.isAdmin() && !user.isAdmin()) {
                    return new Error("IllegalChange", "You do not have the rights to modify your permissions!");
                }
                // password
                if (safeString(sentUser.getPwdHash())) {
                    // hash:
                    user.setPwdHash(BCrypt.hashpw(sentUser.getPwdHash(), BCrypt.gensalt(12)));
                }
                // name
                if (safeString(sentUser.getName())) {
                    user.setName(sentUser.getName());
                }
                // Note that the session user object now needs to be updated. This is done the next time the user
                // sends a request through SecurityModule; it will always get the up to date object from the
                // database.
                return moduleManager.handleTask(Task.User.UPDATE, user);
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
                // find the area
                Data data = moduleManager.handleTask(Task.Position.FIND, arrival.getObject());
                Data msg = checkDataMessage(data, Area.class);
                if (msg != null) {
                    return msg;
                }
                Area area = ((Area) data);
                // update user for position
                user.setLastPosition(area.getID());
                msg = moduleManager.handleTask(Task.User.UPDATE, user);
                if (!(msg instanceof Success)) {
                    return msg;
                }
                // everything okay, return area
                return area;
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
    public Data handleAdminTask(Arrival arrival, User user) {
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
        Area area;
        switch (task) {
            // USERS -----------------------------------------------------------------------------
            case ADMIN_USER_READ:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                data = moduleManager.handleTask(Task.User.READ, arrival.getObject());
                message = checkDataMessage(data, DataList.class);
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
                // check email
                if (!safeString(tempUser.getEmail())) {
                    return new Error("IllegalAdd", "Email is primary key! May not be empty.");
                }
                // for security reasons, log this
                if (tempUser.isAdmin()) {
                    log.log(TAG, "Adding user " + tempUser.getEmail() + " as admin!");
                }
                // all else we set manually to valid values
                tempUser.setLastPosition(null);
                tempUser.setLastAccess(null);
                // check & handle password (we do this last because we might need to send back the key)
                if (safeString(tempUser.getPwdHash())) {
                    // this means a password was provided
                    tempUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
                    // update to DB
                    return moduleManager.handleTask(Task.User.CREATE, tempUser);
                }
                // this means we generate a password
                String key = generateKey();
                // hash it
                tempUser.setPwdHash(BCrypt.hashpw(key, BCrypt.gensalt(12)));
                // update to DB
                data = moduleManager.handleTask(Task.User.CREATE, tempUser);
                // Only send the key if the update was successful
                if (data instanceof Success) {
                    return new Message("AddUserSuccessKey", key);
                }
                // send the error
                return nullMessageCatch(data);
            case ADMIN_USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                tempUser = (User) arrival.getObject();
                // check email
                if (!safeString(tempUser.getEmail())) {
                    return new Error("IllegalChange", "Email must not be empty!");
                }
                // get original
                data = moduleManager.handleTask(Task.User.READ, new User(tempUser.getEmail()));
                message = checkDataMessage(data, DataList.class);
                if (message != null) {
                    return message;
                }
                // make sure we get one back
                if (((DataList) data).size() != 1) {
                    return new Error("UserUpdateFailed", "User " + tempUser.getEmail() + " not found!");
                }
                User originalUser = (User) ((DataList) data).get(0);
                // change name
                if (safeString(tempUser.getName())) {
                    originalUser.setName(tempUser.getName());
                }
                // change password
                if (safeString(tempUser.getPwdHash())) {
                    originalUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
                }
                // change admin status
                originalUser.setAdmin(tempUser.isAdmin());
                // and update
                return moduleManager.handleTask(Task.User.UPDATE, originalUser);
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
                Location location = (Location) arrival.getObject();
                if (location.getWifiMorsels().isEmpty()) {
                    log.error(TAG, "WARNING: Adding location at " + location.getCoordinateX() + "|" + location.getCoordinateY() + " with EMPTY MORSELS!");
                }
                return moduleManager.handleTask(Task.Location.CREATE, location);
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
                // Adding locations via area_add is not allowed
                area = (Area) arrival.getObject();
                if (area.getLocations() != null) {
                    return new Error("IllegalAreaAdd", "Adding locations via an area is illegal!");
                }
                return moduleManager.handleTask(Task.Area.CREATE, area);
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
                // check identification
                if (!safeString(display.getIdentification())) {
                    return new Error("IllegalAdd", "Identification is primary key! May not be empty.");
                }
                // check coordinates
                if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
                    return new Error("IllegalAdd", "Coordinates must be positive!");
                }
                // password
                if (safeString(display.getToken())) {
                    display.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
                    return moduleManager.handleTask(Task.Display.CREATE, display);
                }
                // otherwise we need to generate a token
                String token = generateKey();
                // hash it
                display.setToken(BCrypt.hashpw(token, BCrypt.gensalt(12)));
                data = moduleManager.handleTask(Task.Display.CREATE, display);
                // If the operation was a success, we need to send the generated key back
                if (data instanceof Success) {
                    data = new Message("DisplayAddSuccessKey", token);
                }
                return nullMessageCatch(data);
            case DISPLAY_UPDATE:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                display = (PublicDisplay) arrival.getObject();
                if (!safeString(display.getIdentification())) {
                    return new Error("IllegalChange", "Identification must not be empty!");
                }
                data = moduleManager.handleTask(Task.Display.READ, new PublicDisplay(display.getIdentification(), null, null, 0, 0));
                message = checkDataMessage(data, DataList.class);
                if (message != null) {
                    return message;
                }
                // make sure we get one back
                if (((DataList) data).size() != 1) {
                    return new Error("DisplayUpdateFailed", "Display " + display.getIdentification() + " not found!");
                }
                PublicDisplay originalDisplay = (PublicDisplay) ((DataList) data).get(0);
                // check coordinates
                if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
                    return new Error("IllegalUpdate", "Coordinates must be positive!");
                }
                // check password
                if (safeString(display.getToken())) {
                    originalDisplay.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
                }
                // check location
                if (safeString(display.getLocation())) {
                    originalDisplay.setLocation(display.getLocation());
                }
                // update
                return moduleManager.handleTask(Task.Display.UPDATE, display);
            case DISPLAY_REMOVE:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Display.DELETE, arrival.getObject());
            // Special admin stuff ---------------------------------------------------------------
            case READ_ALL_ADMIN:
                User filter = new User(null);
                filter.setAdmin(true);
                return moduleManager.handleTask(Task.User.READ, filter);
            case ADMIN_ANNIHILATE_AREA:
                log.log(TAG, "Removing all area objects!");
                return moduleManager.handleTask(Task.Area.ANNIHILATE, null);
            case ADMIN_ANNIHILATE_USER:
                log.log(TAG, "Removing all users!");
                return moduleManager.handleTask(Task.User.ANNIHILATE, null);
            // Read all positions (as publicly seen)
            case READ_ALL_POSITIONS:
                return moduleManager.handleTask(Task.Position.READ, null);
            default:
                return null;
        }
    }

    /**
     * Tasks for PublicDisplays.
     *
     * @param arrival The arrival object.
     * @param display The PublicDisplay user object.
     * @return Resulting data.
     */
    public Data handleDisplayTask(Arrival arrival, PublicDisplay display) {
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        switch (task) {
            case READ_ALL_POSITIONS:
                return moduleManager.handleTask(Task.Position.READ, null);
            case READ_ALL_AREAS:
                Area filter = new Area(null);
                // todo filter these maybe?
                return moduleManager.handleTask(Task.Area.READ, filter);
            default:
                return null;
        }
    }

    /**
     * Method that checks data returned from a module. The method returns null except when: data is an Information
     * interface type class, data is null, or data is not of type class.
     *
     * @param data  The data to check.
     * @param clazz The class type against which to check.
     * @return An Information object if data is such, else null.
     */
    public Information checkDataMessage(Data data, Class clazz) {
        if (data == null) {
            return new Error("DATA NULL", "Data requested returned NULL, should NOT HAPPEN!");
        } else if (data instanceof Information) {
            return (Information) data;
        } else if (data.getClass() != clazz && !clazz.isAssignableFrom(data.getClass())) {
            return new Error("DATA CAST FAILED", "Returned data failed class test!");
        } else {
            // This means everything was okay
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

    /**
     * Small method for checking the validity of input strings.
     *
     * @param toCheck The string to check.
     * @return Boolean value whether legal stuff is happening.
     */
    private boolean safeString(String toCheck) {
        return (toCheck != null && !toCheck.isEmpty());
    }

    /**
     * Method for generating a pseudo-random hash. Note that GENERATED_KEY_LENGTH variable controls how long the key
     * will be at maximum. The key may be shorter though!
     *
     * @return The generated hash.
     */
    private String generateKey() {
        String key = new BigInteger(130, random).toString(32);
        if (key.length() > GENERATED_KEY_LENGTH) {
            return key.substring(0, GENERATED_KEY_LENGTH);
        }
        return key;
    }
}
