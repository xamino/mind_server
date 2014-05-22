package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;

import javax.servlet.ServletContext;

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
    private Messenger log;
    private Configuration config;

    private ServletFunctions(ServletContext context) {
        log = Messenger.getInstance();
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
//
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