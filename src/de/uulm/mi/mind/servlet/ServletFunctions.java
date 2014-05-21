package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
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
    private final int GENERATED_KEY_LENGTH = 8;
    private final String LAST_POSITION = "lastPosition";
    private final String REAL_POSITION = "realPosition";
    private Messenger log;
    private FilePath filePath;
    private Configuration config;

    private ServletFunctions(ServletContext context) {
        log = Messenger.getInstance();
        filePath = new FilePath(context);
        config = Configuration.getInstance();
        // print values we need for debug
        log.log(TAG, "REGISTRATION_POLICY = " + config.getRegistration());
        log.log(TAG, "Created.");
    }

    public static ServletFunctions getInstance(ServletContext context) {
        if (INSTANCE == null) {
            INSTANCE = new ServletFunctions(context);
        }
        return INSTANCE;
    }

//
//    /**
//     * Handles all generally accessible tasks that only require a valid user.
//     *
//     * @param arrival The arrival object.
//     * @return The return value.
//     */
//    public Data handleNormalTask(Arrival arrival, Active activeUser) {
//        // better be safe
//        if (!(activeUser.getAuthenticated() instanceof User)) {
//            log.error(TAG, "Normal task was handed the wrong type of Authenticated!");
//            return new Error(Error.Type.WRONG_OBJECT, "Normal task was handed the wrong type of Authenticated!");
//        }
//        User user = (User) activeUser.getAuthenticated();
//        API task = API.safeValueOf(arrival.getTask());
//        switch (task) {
//            case POSITION_FIND:
//                // find the area
//                Data data = moduleManager.handleTask(Task.Position.FIND, arrival);
//                Data msg = checkDataMessage(data, Area.class);
//                if (msg != null) {
//                    return msg;
//                }
//                Area area = ((Area) data);
//                // pull these out here to make checking if they exist easier
//                Area last = ((Area) activeUser.readData(LAST_POSITION));
//                Area real = ((Area) activeUser.readData(REAL_POSITION));
//                // this implements server-side fuzziness to avoid fluttering of position_find
//                if (last == null || real == null) {
//                    // this means it is the first time in this session, so we don't apply fuzziness
//                    activeUser.writeData(LAST_POSITION, area);
//                    activeUser.writeData(REAL_POSITION, area);
//                    user.setPosition(area.getID());
//                } else if (last.getID().equals(area.getID())) {
//                    // update user for position, but only if last was already the same and the previous db entry is different
//                    activeUser.writeData(REAL_POSITION, area);
//                    user.setPosition(area.getID());
//                } else {
//                    // this means the area is different than the one before, so change last but not real:
//                    activeUser.writeData(LAST_POSITION, area);
//                }
//                // everything okay, return real position area (must be freshly read because we might have written to it)
//                return (Area) activeUser.readData(REAL_POSITION);
//            case ICON_DELETE:
//                String icon = "icon_" + user.readIdentification();
//                File file = new File(filePath.iconPath() + icon);
//                if (!file.exists()) {
//                    return new Success(Success.Type.NOTE, "No icon to remove.");
//                }
//                if (!file.delete()) {
//                    log.error(TAG, "Failed to remove icon for " + user.readIdentification() + "!");
//                    return new Error(Error.Type.SERVER, "Failed to delete icon!");
//                }
//                log.log(TAG, "User " + user.readIdentification() + " deleted icon.");
//                return new Success("Icon removed.");
//            default:
//                return null;
//        }
//    }
//
//            case ADMIN_USER_ADD:
//                if (!(arrival.getObject() instanceof User)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                tempUser = (User) arrival.getObject();
//                // check email
//                if (!safeString(tempUser.getEmail())) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Email is primary key! May not be empty.");
//                }
//                // for security reasons, log this
//                if (tempUser.isAdmin()) {
//                    log.log(TAG, "Adding user " + tempUser.getEmail() + " as admin!");
//                }
//                // all else we set manually to valid values
//                tempUser.setPosition(null);
//                tempUser.setAccessDate(null);
//                // check & handle password (we do this last because we might need to send back the key)
//                if (safeString(tempUser.getPwdHash())) {
//                    // this means a password was provided
//                    tempUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
//                    // update to DB
//                    return moduleManager.handleTask(Task.User.CREATE, tempUser);
//                }
//                // this means we generate a password
//                String key = generateKey();
//                // hash it
//                tempUser.setPwdHash(BCrypt.hashpw(key, BCrypt.gensalt(12)));
//                // update to DB
//                data = moduleManager.handleTask(Task.User.CREATE, tempUser);
//                // Only send the key if the update was successful
//                if (data instanceof Success) {
//                    return new Success(Success.Type.NOTE, key);
//                }
//                // send the error
//                return nullMessageCatch(data);
//            case ADMIN_USER_UPDATE:
//                if (!(arrival.getObject() instanceof User)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                tempUser = (User) arrival.getObject();
//                // check email
//                if (!safeString(tempUser.getEmail())) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Email must not be empty!");
//                }
//                // get original
//                data = moduleManager.handleTask(Task.User.READ, new User(tempUser.getEmail()));
//                message = checkDataMessage(data, DataList.class);
//                if (message != null) {
//                    return message;
//                }
//                // make sure we get one back
//                if (((DataList) data).size() != 1) {
//                    return new Error(Error.Type.DATABASE, "User " + tempUser.getEmail() + " not found!");
//                }
//                User originalUser = (User) ((DataList) data).get(0);
//                // change name
//                if (safeString(tempUser.getName())) {
//                    originalUser.setName(tempUser.getName());
//                }
//                // change password
//                if (safeString(tempUser.getPwdHash())) {
//                    originalUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
//                }
//                // change status
//                originalUser.setStatus(tempUser.getStatus());
//                // change admin status
//                originalUser.setAdmin(tempUser.isAdmin());
//                // and update
//                return moduleManager.handleTask(Task.User.UPDATE, originalUser);
//            // LOCATION --------------------------------------------------------------------------
//            // TODO sanitize and make sane!
//            case LOCATION_ADD:
//                if (!(arrival.getObject() instanceof Location)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                Location location = (Location) arrival.getObject();
//                if (location.getWifiMorsels().isEmpty()) {
//                    log.error(TAG, "NOTE: Adding location at " + location.getCoordinateX() + "|" + location.getCoordinateY() + " with EMPTY MORSELS!");
//                }
//                return moduleManager.handleTask(Task.Location.CREATE, location);
//            case LOCATION_REMOVE:
//                if (!(arrival.getObject() instanceof Location)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                return moduleManager.handleTask(Task.Location.DELETE, arrival.getObject());
//            // AREAS -----------------------------------------------------------------------------
//            // TODO sanitize and make sane!
//            case AREA_UPDATE:
//                if (!(arrival.getObject() instanceof Area)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                return moduleManager.handleTask(Task.Area.UPDATE, arrival.getObject());
//            case AREA_REMOVE:
//                if (!(arrival.getObject() instanceof Area)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                return moduleManager.handleTask(Task.Area.DELETE, arrival.getObject());
//            // DISPLAYS ---------------------------------------------------------------------------
//            case DISPLAY_UPDATE:
//                if (!(arrival.getObject() instanceof PublicDisplay)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                display = (PublicDisplay) arrival.getObject();
//                if (!safeString(display.getIdentification())) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
//                }
//                data = moduleManager.handleTask(Task.Display.READ, new PublicDisplay(display.getIdentification(), null, null, 0, 0));
//                message = checkDataMessage(data, DataList.class);
//                if (message != null) {
//                    return message;
//                }
//                // make sure we get one back
//                if (((DataList) data).size() != 1) {
//                    return new Error(Error.Type.DATABASE, "Display " + display.getIdentification() + " not found!");
//                }
//                PublicDisplay originalDisplay = (PublicDisplay) ((DataList) data).get(0);
//                // check coordinates
//                if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Coordinates must be positive!");
//                }
//                // check password
//                if (safeString(display.getToken())) {
//                    originalDisplay.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
//                }
//                // check location
//                if (safeString(display.getLocation())) {
//                    originalDisplay.setLocation(display.getLocation());
//                }
//                // update
//                return moduleManager.handleTask(Task.Display.UPDATE, display);
//            // SENSORS -----------------------------------------------------------------------------------------
//            case SENSOR_ADD:
//                if (!(arrival.getObject() instanceof WifiSensor)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                sensor = (WifiSensor) arrival.getObject();
//                // check identification
//                if (!safeString(sensor.readIdentification())) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification is primary key! May not be empty.");
//                }
//                // password
//                if (safeString(sensor.getTokenHash())) {
//                    sensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
//                    return moduleManager.handleTask(Task.Sensor.CREATE, sensor);
//                }
//                // otherwise we need to generate a token
//                token = generateKey();
//                // hash it
//                sensor.setTokenHash(BCrypt.hashpw(token, BCrypt.gensalt(12)));
//                data = moduleManager.handleTask(Task.Sensor.CREATE, sensor);
//                // If the operation was a success, we need to send the generated key back
//                if (data instanceof Success) {
//                    data = new Success(Success.Type.NOTE, token);
//                }
//                return nullMessageCatch(data);
//            case SENSOR_UPDATE:
//                if (!(arrival.getObject() instanceof WifiSensor)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                sensor = (WifiSensor) arrival.getObject();
//                if (!safeString(sensor.readIdentification())) {
//                    return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
//                }
//                data = moduleManager.handleTask(Task.Sensor.READ, new WifiSensor(sensor.readIdentification(), null, null));
//                message = checkDataMessage(data, DataList.class);
//                if (message != null) {
//                    return message;
//                }
//                // make sure we get one back
//                if (((DataList) data).size() != 1) {
//                    return new Error(Error.Type.DATABASE, "WifiSensor " + sensor.readIdentification() + " not found!");
//                }
//                WifiSensor originalSensor = (WifiSensor) ((DataList) data).get(0);
//                // check password
//                if (safeString(sensor.getTokenHash())) {
//                    originalSensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
//                }
//                if (safeString(sensor.getArea())) {
//                    originalSensor.setArea(sensor.getArea());
//                }
//                // update
//                return moduleManager.handleTask(Task.Sensor.UPDATE, originalSensor);
//            // Special admin stuff ---------------------------------------------------------------
//            case READ_ALL_ADMIN:
//                User filter = new User(null);
//                filter.setAdmin(true);
//                return moduleManager.handleTask(Task.User.READ, filter);
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * Tasks for the WifiSensors.
//     *
//     * @param arrival The arrival object.
//     * @param activeUser The activeUser object.
//     * @return The value to return.
//     */
//    public Data handleWifiSensorTask(Arrival arrival, Active activeUser) {
//        // better be safe
//        if (!(activeUser.getAuthenticated() instanceof WifiSensor)) {
//            log.error(TAG, "WifiSensor task was handed the wrong type of Authenticated!");
//            return new Error(Error.Type.WRONG_OBJECT, "WifiSensor task was handed the wrong type of Authenticated!");
//        }
//        WifiSensor sensor = ((WifiSensor) activeUser.getAuthenticated());
//        API task = API.safeValueOf(arrival.getTask());
//        switch (task) {
//            case WIFI_SENSOR_UPDATE:
//                if (!(arrival.getObject() instanceof DataList)) {
//                    return new Error(Error.Type.WRONG_OBJECT);
//                }
//                // check to make sure the sensor has a registered position
//                if (sensor.getArea() == null || sensor.getArea().isEmpty()) {
//                    log.error(TAG, "WifiSensor " + sensor.readIdentification() + " has no area registered to it!");
//                    return new Error(Error.Type.ILLEGAL_VALUE, "WifiSensor " + sensor.readIdentification() + " " +
//                            "has no area registered to it! Ignoring devices.");
//                }
//                DataList<SensedDevice> devices = ((DataList) arrival.getObject());
//                // no need to continue if empty
//                if (devices.isEmpty()) {
//                    return new Success(Success.Type.NOTE, "Operation okay, but empty device list!");
//                }
//                // security check && fill in information
//                for (SensedDevice device : devices) {
//                    if (!device.getSensor().equals(sensor.readIdentification())) {
//                        // make sure a sensor only updates the devices for its own location
//                        return new Error(Error.Type.ILLEGAL_VALUE, "Sensor device injection is illegal!");
//                    }
//                    // store position of sensor for easy access later on
//                    device.setPosition(sensor.getArea());
//                }
//                // pass tasks down
//                return moduleManager.handleTask(Task.Position.SENSOR_WRITE, devices);
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * Method for generating a pseudo-random hash. Note that GENERATED_KEY_LENGTH variable controls how long the key
//     * will be at maximum. The key may be shorter though!
//     *
//     * @return The generated hash.
//     */
//    private String generateKey() {
//        String key = new BigInteger(130, new SecureRandom()).toString(32);
//        if (key.length() > GENERATED_KEY_LENGTH) {
//            return key.substring(0, GENERATED_KEY_LENGTH);
//        }
//        return key;
//    }
}