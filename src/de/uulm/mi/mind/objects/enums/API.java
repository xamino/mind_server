package de.uulm.mi.mind.objects.enums;

/**
 * @author Tamino Hartmann
 *         This enum represents the actual, external API that can be called. Well, this and Security. All tasks
 *         must be listed here, with collision free names. Tasks should start with the name of their module, then
 *         continue with an expressive name that describes what is done.
 */
public enum API implements Task {
    // LOCATION
    LOCATION_READ,
    LOCATION_ADD,
    // LOCATION_UPDATE,
    LOCATION_REMOVE,
    // AREA
    AREA_READ,
    AREA_ADD,
    AREA_UPDATE,
    AREA_REMOVE,
    // Display
    DISPLAY_READ,
    DISPLAY_ADD,
    DISPLAY_UPDATE,
    DISPLAY_REMOVE,
    // Sensor
    SENSOR_READ,
    SENSOR_ADD,
    SENSOR_UPDATE,
    SENSOR_REMOVE,
    /**
     * Given a location, find the position.
     */
    POSITION_FIND,
    /**
     * A read all areas function for the public displays.
     */
    READ_ALL_AREAS,
    // ADMIN
    READ_ALL_ADMIN,
    ADMIN_USER_READ,
    ADMIN_USER_ADD,
    ADMIN_USER_UPDATE,
    ADMIN_USER_DELETE,
    /**
     * Removes all areas and locations from the database and restores the default area
     */
    ADMIN_ANNIHILATE_AREA,
    /**
     * Removes all users from the database and restores the default admin
     */
    ADMIN_ANNIHILATE_USER,
    /**
     * Returns a list of all Active objects currently within the system.
     */
    ADMIN_READ_SESSIONS,
    TOGGLE_ADMIN,
    /**
     * Updates the list of devices for a given WifiSensor.
     */
    WIFI_SENSOR_UPDATE,
    /**
     * Deletes a users icon from the system.
     */
    ICON_DELETE
}