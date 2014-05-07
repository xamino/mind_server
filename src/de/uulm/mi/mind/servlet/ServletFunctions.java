package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Authenticated;
import de.uulm.mi.mind.security.BCrypt;
import de.uulm.mi.mind.security.Security;

import javax.servlet.ServletContext;
import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

/**
 * @author Tamino Hartmann
 *         Class for handling all the serious tasks of the servlet. Mainly there to keep Servlet comparatively clean.
 */
// TODO reimplement tasks as anonymous interface objects with class[] info for who may access them
public class ServletFunctions {
    private static ServletFunctions INSTANCE;
    private final String TAG = "ServletFunctions";
    /**
     * This controls the length of the generated keys when adding users or displays without a preset password.
     */
    private final int GENERATED_KEY_LENGTH = 6;
    private final String LAST_POSITION = "lastPosition";
    private final String REAL_POSITION = "realPosition";
    private Messenger log;
    private EventModuleManager moduleManager;
    private SecureRandom random;
    private FilePath filePath;

    private ServletFunctions(ServletContext context) {
        log = Messenger.getInstance();
        moduleManager = EventModuleManager.getInstance();
        random = new SecureRandom();
        filePath = new FilePath(context);
        log.log(TAG, "Created.");
    }

    public static ServletFunctions getInstance(ServletContext context) {
        if (INSTANCE == null) {
            INSTANCE = new ServletFunctions(context);
        }
        return INSTANCE;
    }

    /**
     * Method that checks data returned from a module. The method returns null except when: data is an Information
     * interface type class, data is null, or data is not of type class.
     *
     * @param data  The data to check.
     * @param clazz The class type against which to check.
     * @return An Information object if data is such, else null.
     */
    public static Information checkDataMessage(Data data, Class clazz) {
        if (data == null) {
            return new Error(Error.Type.NULL, "Data requested returned NULL, should NOT HAPPEN!");
        } else if (data instanceof Information) {
            return (Information) data;
        } else if (data.getClass() != clazz && !clazz.isAssignableFrom(data.getClass())) {
            return new Error(Error.Type.CAST, "Returned data failed class test!");
        } else {
            // This means everything was okay
            return null;
        }
    }

    /**
     * Handles most public tasks, most importantly security functions.
     *
     * @param arrival
     * @return Null if no task here was done, otherwise the message to send back.
     */
    public Information handlePublicTask(Arrival arrival) {
        Active activeUser;
        API task = API.safeValueOf(arrival.getTask());
        switch (task) {
            case LOGIN:
                // make sure we have the right object
                if (!(arrival.getObject() instanceof Authenticated)) {
                    return new Error(Error.Type.WRONG_OBJECT, "Login requires a User, PublicDisplay, or WifiSensor object!");
                }
                // try login
                activeUser = Security.begin((Authenticated) arrival.getObject(), null);
                // check if okay
                if (activeUser == null) {
                    return new Error(Error.Type.LOGIN, "Login failed. Check identification, authentication, and user type!");
                }
                // otherwise we finish again directly by returning the session
                Security.finish(activeUser);
                // If it was the first login, we send a note instead of just a simple ok so the client can know
                if (activeUser.wasUnused()) {
                    return new Success(Success.Type.NOTE, activeUser.getSESSION());
                }
                return new Success(Success.Type.OK, activeUser.getSESSION());
            case LOGOUT:
                activeUser = Security.begin(null, arrival.getSessionHash());
                if (activeUser == null) {
                    return new Success(Success.Type.NOTE, "Session is already not valid!");
                }
                activeUser.invalidate();
                Security.finish(activeUser);
                return new Success("Logout successful.");
            case CHECK:
                activeUser = Security.begin(null, arrival.getSessionHash());
                if (activeUser == null) {
                    return new Error(Error.Type.SECURITY, "Session invalid!");
                }
                Security.finish(activeUser);
                return new Success("Session is valid.");
            case REGISTRATION:
                // Public registration is only available to normal users!
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT, "Registration is only possible with User objects!");
                }
                User user = ((User) arrival.getObject());
                if (user.readIdentification().isEmpty() || user.readAuthentication().isEmpty()) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Email and password may not be empty!");
                }
                // hash pwd
                user.setPwdHash(BCrypt.hashpw(user.getPwdHash(), BCrypt.gensalt(12)));
                // set access time
                user.setAccessDate(new Date());
                // create user
                Data msg = EventModuleManager.getInstance().handleTask(Task.User.CREATE, user);
                // check database reply
                if (msg instanceof Success) {
                    log.log(TAG, "User " + user.readIdentification() + " has been registered.");
                    return new Success("Registered to '" + user.getEmail() + "'.");
                }
                log.error(TAG, "Error creating a user!");
                return (Information) msg;
            default:
                return null;
        }
    }

    /**
     * Handles all generally accessible tasks that only require a valid user.
     *
     * @param arrival
     * @return
     */
    public Data handleNormalTask(Arrival arrival, Active activeUser) {
        // better be safe
        if (!(activeUser.getAuthenticated() instanceof User)) {
            log.error(TAG, "Normal task was handed the wrong type of Authenticated!");
            return new Error(Error.Type.WRONG_OBJECT, "Normal task was handed the wrong type of Authenticated!");
        }
        User user = (User) activeUser.getAuthenticated();
        API task = API.safeValueOf(arrival.getTask());
        switch (task) {
            case ECHO:
                // Simple echo test for checking if the server can parse the data
                return arrival.getObject();
            case USER_READ:
                // NOTE: do NOT allow ANY USER TO BE READ HERE – SECURITY LEAK!
                // Admin should use admin_user_read!
                return user.safeClone();
            case USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                User sentUser = (User) arrival.getObject();
                // Email is primary key, thus can not be changed!
                if (!sentUser.getEmail().equals(user.getEmail())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Email can not be changed in an existing user");
                }
                // Make sure that you can't set or unset yourself to admin
                if (sentUser.isAdmin() && !user.isAdmin()) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "You do not have the rights to modify your permissions!");
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
                // status TODO remove log when working
                log.log(TAG, "User update status set to: " + sentUser.getStatus());
                user.setStatus(sentUser.getStatus());
                // Note that the session user object now needs to be updated. This is done the next time the user
                // sends a request through SecurityModule; it will always get the up to date object from the
                // database.
                return moduleManager.handleTask(Task.User.UPDATE, user);
            case USER_DELETE:
                // To catch some errors, we enforce that no object has been passed along:
                if (arrival.getObject() != null) {
                    // If null...
                    return new Error(Error.Type.WRONG_OBJECT, "To delete your user, you must not pass any object along.");
                }
                // Remove session when done
                activeUser.invalidate();
                // Otherwise ignore all else, just delete:
                return moduleManager.handleTask(Task.User.DELETE, user);
            case POSITION_FIND:
                // find the area
                Data data = moduleManager.handleTask(Task.Position.FIND, arrival);
                Data msg = checkDataMessage(data, Area.class);
                if (msg != null) {
                    return msg;
                }
                Area area = ((Area) data);
                boolean areaChanged = false;
                // todo user should not save area again, could overwrite!!!
                // this implements server-side fuzziness to avoid fluttering of position_find
                if (!(activeUser.readData(LAST_POSITION) instanceof Area)) {
                    // this means it is the first time in this session, so we don't apply fuzziness
                    activeUser.writeData(LAST_POSITION, area);
                    activeUser.writeData(REAL_POSITION, area);
                    user.setPosition(area.getID());
                    areaChanged = true;
                } else if (((Area) activeUser.readData(LAST_POSITION)).getID().equals(area.getID())
                        && !user.getPosition().equals(area.getID())) {
                    // update user for position, but only if last was already the same and the previous db entry is different
                    activeUser.writeData(REAL_POSITION, area);
                    user.setPosition(area.getID());
                    areaChanged = true;
                } else {
                    // this means the area is different than the one before, so change lastPosition but not User:
                    activeUser.writeData(LAST_POSITION, area);
                }
                // Only update user if location has actually changed.
                if (areaChanged) {
                    log.log(TAG, "Area changed update user!");
                    msg = moduleManager.handleTask(Task.User.UPDATE, user);
                    if (!(msg instanceof Success)) {
                        return msg;
                    }
                }
                // everything okay, return real position area
                return (Data) activeUser.readData(REAL_POSITION);
            case TOGGLE_ADMIN:
                // TODO remove this, only for test!
                log.error(TAG, "Toggled admin! DANGEROUS OPERATION!");
                user.setAdmin(!user.isAdmin());
                return moduleManager.handleTask(Task.User.UPDATE, user);
            case READ_ALL_POSITIONS:
                return moduleManager.handleTask(Task.Position.READ, null);
            case READ_ALL_AREAS:
                return moduleManager.handleTask(Task.Area.READ, new Area(null));
            case ICON_DELETE:
                String icon = "icon_" + user.readIdentification();
                File file = new File(filePath.iconPath() + icon);
                if (!file.exists()) {
                    return new Success(Success.Type.NOTE, "No icon to remove.");
                }
                if (!file.delete()) {
                    log.error(TAG, "Failed to remove icon for " + user.readIdentification() + "!");
                    return new Error(Error.Type.SERVER, "Failed to delete icon!");
                }
                log.log(TAG, "User " + user.readIdentification() + " deleted icon.");
                return new Success("Icon removed.");
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
    public Data handleAdminTask(Arrival arrival, Active activeUser) {
        // better be safe
        if (!(activeUser.getAuthenticated() instanceof User)) {
            log.error(TAG, "Administrative task was handed the wrong type of Authenticated!");
            return new Error(Error.Type.WRONG_OBJECT, "Administrative task was handed the wrong type of Authenticated!");
        }
        User user = (User) activeUser.getAuthenticated();
        // Better safe than sorry:
        if (!user.isAdmin()) {
            log.error(TAG, "User " + user.readIdentification() + " almost accessed admin functions!");
            return new Error(Error.Type.SECURITY, "You do not have permission for this task.");
        }
        API task = API.safeValueOf(arrival.getTask());
        // Because we'll need these rather often:
        Data data, message;
        User tempUser;
        PublicDisplay display;
        WifiSensor sensor;
        String token;
        Area area;
        switch (task) {
            // USERS -----------------------------------------------------------------------------
            case ADMIN_USER_READ:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                data = moduleManager.handleTask(Task.User.READ, arrival.getObject());
                message = checkDataMessage(data, DataList.class);
                if (message == null) {
                    DataList<User> users = new DataList<>();
                    DataList<User> fromDB = ((DataList<User>) data);
                    for (User bum : fromDB) {
                        users.add(bum.safeClone());
                    }
                    return users;
                }
                return nullMessageCatch(message);
            case ADMIN_USER_ADD:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                tempUser = (User) arrival.getObject();
                // check email
                if (!safeString(tempUser.getEmail())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Email is primary key! May not be empty.");
                }
                // for security reasons, log this
                if (tempUser.isAdmin()) {
                    log.log(TAG, "Adding user " + tempUser.getEmail() + " as admin!");
                }
                // all else we set manually to valid values
                tempUser.setPosition(null);
                tempUser.setAccessDate(null);
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
                    return new Success(Success.Type.NOTE, key);
                }
                // send the error
                return nullMessageCatch(data);
            case ADMIN_USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                tempUser = (User) arrival.getObject();
                // check email
                if (!safeString(tempUser.getEmail())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Email must not be empty!");
                }
                // get original
                data = moduleManager.handleTask(Task.User.READ, new User(tempUser.getEmail()));
                message = checkDataMessage(data, DataList.class);
                if (message != null) {
                    return message;
                }
                // make sure we get one back
                if (((DataList) data).size() != 1) {
                    return new Error(Error.Type.DATABASE, "User " + tempUser.getEmail() + " not found!");
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
                // change status
                originalUser.setStatus(tempUser.getStatus());
                // change admin status
                originalUser.setAdmin(tempUser.isAdmin());
                // and update
                return moduleManager.handleTask(Task.User.UPDATE, originalUser);
            case ADMIN_USER_DELETE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.User.DELETE, arrival.getObject());
            // LOCATION --------------------------------------------------------------------------
            // TODO sanitize and make sane!
            // todo filter for eduroam and welcome
            case LOCATION_READ:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Location.READ, arrival.getObject());
            case LOCATION_ADD:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                Location location = (Location) arrival.getObject();
                if (location.getWifiMorsels().isEmpty()) {
                    log.error(TAG, "NOTE: Adding location at " + location.getCoordinateX() + "|" + location.getCoordinateY() + " with EMPTY MORSELS!");
                }
                return moduleManager.handleTask(Task.Location.CREATE, location);
            case LOCATION_REMOVE:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Location.DELETE, arrival.getObject());
            // AREAS -----------------------------------------------------------------------------
            // TODO sanitize and make sane!
            case AREA_READ:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Area.READ, arrival.getObject());
            case AREA_ADD:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                // Adding locations via area_add is not allowed
                area = (Area) arrival.getObject();
                if (area.getLocations() != null) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Adding locations via an area is illegal!");
                }
                return moduleManager.handleTask(Task.Area.CREATE, area);
            case AREA_UPDATE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Area.UPDATE, arrival.getObject());
            case AREA_REMOVE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Area.DELETE, arrival.getObject());
            // DISPLAYS ---------------------------------------------------------------------------
            case DISPLAY_READ:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Display.READ, arrival.getObject());
            case DISPLAY_ADD:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                display = (PublicDisplay) arrival.getObject();
                // check identification
                if (!safeString(display.getIdentification())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification is primary key! May not be empty.");
                }
                // check coordinates
                if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Coordinates must be positive!");
                }
                // password
                if (safeString(display.getToken())) {
                    display.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
                    return moduleManager.handleTask(Task.Display.CREATE, display);
                }
                // otherwise we need to generate a token
                token = generateKey();
                // hash it
                display.setToken(BCrypt.hashpw(token, BCrypt.gensalt(12)));
                data = moduleManager.handleTask(Task.Display.CREATE, display);
                // If the operation was a success, we need to send the generated key back
                if (data instanceof Success) {
                    data = new Success(Success.Type.NOTE, token);
                }
                return nullMessageCatch(data);
            case DISPLAY_UPDATE:
                if (!(arrival.getObject() instanceof PublicDisplay)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                display = (PublicDisplay) arrival.getObject();
                if (!safeString(display.getIdentification())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
                }
                data = moduleManager.handleTask(Task.Display.READ, new PublicDisplay(display.getIdentification(), null, null, 0, 0));
                message = checkDataMessage(data, DataList.class);
                if (message != null) {
                    return message;
                }
                // make sure we get one back
                if (((DataList) data).size() != 1) {
                    return new Error(Error.Type.DATABASE, "Display " + display.getIdentification() + " not found!");
                }
                PublicDisplay originalDisplay = (PublicDisplay) ((DataList) data).get(0);
                // check coordinates
                if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Coordinates must be positive!");
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
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Display.DELETE, arrival.getObject());
            // SENSORS -----------------------------------------------------------------------------------------
            case SENSOR_READ:
                if (!(arrival.getObject() instanceof WifiSensor)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Sensor.READ, arrival.getObject());
            case SENSOR_ADD:
                if (!(arrival.getObject() instanceof WifiSensor)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                sensor = (WifiSensor) arrival.getObject();
                // check identification
                if (!safeString(sensor.readIdentification())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification is primary key! May not be empty.");
                }
                // password
                if (safeString(sensor.getTokenHash())) {
                    sensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
                    return moduleManager.handleTask(Task.Sensor.CREATE, sensor);
                }
                // otherwise we need to generate a token
                token = generateKey();
                // hash it
                sensor.setTokenHash(BCrypt.hashpw(token, BCrypt.gensalt(12)));
                data = moduleManager.handleTask(Task.Sensor.CREATE, sensor);
                // If the operation was a success, we need to send the generated key back
                if (data instanceof Success) {
                    data = new Success(Success.Type.NOTE, token);
                }
                return nullMessageCatch(data);
            case SENSOR_UPDATE:
                if (!(arrival.getObject() instanceof WifiSensor)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                sensor = (WifiSensor) arrival.getObject();
                if (!safeString(sensor.readIdentification())) {
                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
                }
                data = moduleManager.handleTask(Task.Sensor.READ, new WifiSensor(sensor.readIdentification(), null, null));
                message = checkDataMessage(data, DataList.class);
                if (message != null) {
                    return message;
                }
                // make sure we get one back
                if (((DataList) data).size() != 1) {
                    return new Error(Error.Type.DATABASE, "WifiSensor " + sensor.readIdentification() + " not found!");
                }
                WifiSensor originalSensor = (WifiSensor) ((DataList) data).get(0);
                // check password
                if (safeString(sensor.getTokenHash())) {
                    originalSensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
                }
                if (safeString(sensor.getArea())) {
                    originalSensor.setArea(sensor.getArea());
                }
                // update
                return moduleManager.handleTask(Task.Sensor.UPDATE, originalSensor);
            case SENSOR_REMOVE:
                if (!(arrival.getObject() instanceof WifiSensor)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                return moduleManager.handleTask(Task.Sensor.DELETE, arrival.getObject());
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
                // purge sessions
                Security.clear();
                return moduleManager.handleTask(Task.User.ANNIHILATE, null);
            // Read all positions (as publicly seen)
            case READ_ALL_POSITIONS:
                return moduleManager.handleTask(Task.Position.READ, null);
            case ADMIN_READ_SESSIONS:
                return Security.readActives();
            case KILL_SESSIONS:
                Security.clear();
                return new Success("All active sessions have been killed.");
            default:
                return null;
        }
    }

    /**
     * Tasks for PublicDisplays.
     *
     * @param arrival The arrival object.
     * @return Resulting data.
     */
    public Data handleDisplayTask(Arrival arrival, Active activeUser) {
        // better be safe
        if (!(activeUser.getAuthenticated() instanceof PublicDisplay)) {
            log.error(TAG, "Display task was handed the wrong type of Authenticated!");
            return new Error(Error.Type.WRONG_OBJECT, "Display task was handed the wrong type of Authenticated!");
        }
        PublicDisplay display = ((PublicDisplay) activeUser.getAuthenticated());
        API task = API.safeValueOf(arrival.getTask());
        switch (task) {
            case READ_ALL_POSITIONS:
                return moduleManager.handleTask(Task.Position.READ, null);
            case READ_ALL_AREAS:
                Area filter = new Area(null);
                // TODO filter these maybe?
                return moduleManager.handleTask(Task.Area.READ, filter);
            default:
                return null;
        }
    }

    /**
     * Tasks for the WifiSensors.
     *
     * @param arrival
     * @param activeUser
     * @return
     */
    public Data handleWifiSensorTask(Arrival arrival, Active activeUser) {
        // better be safe
        if (!(activeUser.getAuthenticated() instanceof WifiSensor)) {
            log.error(TAG, "WifiSensor task was handed the wrong type of Authenticated!");
            return new Error(Error.Type.WRONG_OBJECT, "WifiSensor task was handed the wrong type of Authenticated!");
        }
        WifiSensor sensor = ((WifiSensor) activeUser.getAuthenticated());
        API task = API.safeValueOf(arrival.getTask());
        switch (task) {
            case WIFI_SENSOR_UPDATE:
                if (!(arrival.getObject() instanceof DataList)) {
                    return new Error(Error.Type.WRONG_OBJECT);
                }
                // check to make sure the sensor has a registered position
                if (sensor.getArea() == null || sensor.getArea().isEmpty()) {
                    log.error(TAG, "WifiSensor " + sensor.readIdentification() + " has no area registered to it!");
                    return new Error(Error.Type.ILLEGAL_VALUE, "WifiSensor " + sensor.readIdentification() + " " +
                            "has no area registered to it! Ignoring devices.");
                }
                DataList<SensedDevice> devices = ((DataList) arrival.getObject());
                // no need to continue if empty
                if (devices.isEmpty()) {
                    return new Success(Success.Type.NOTE, "Operation okay, but empty device list!");
                }
                // security check && fill in information
                for (SensedDevice device : devices) {
                    if (!device.getSensor().equals(sensor.readIdentification())) {
                        // make sure a sensor only updates the devices for its own location
                        return new Error(Error.Type.ILLEGAL_VALUE, "Sensor device injection is illegal!");
                    }
                    // store position of sensor for easy access later on
                    device.setPosition(sensor.getArea());
                }
                // pass tasks down
                return moduleManager.handleTask(Task.Position.SENSOR_WRITE, devices);
            default:
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
            return new Error(Error.Type.NULL, "Something went wrong in the task but no message was written!");
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
