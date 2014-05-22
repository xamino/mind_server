package de.uulm.mi.mind.objects.enums;

/**
 * @author Tamino Hartmann
 *         This enum represents the actual, external API that can be called. Well, this and Security. All tasks
 *         must be listed here, with collision free names. Tasks should start with the name of their module, then
 *         continue with an expressive name that describes what is done.
 */
public enum API implements Task {
    // Sensor
    SENSOR_ADD,
    SENSOR_UPDATE,
    /**
     * Updates the list of devices for a given WifiSensor.
     */
    WIFI_SENSOR_UPDATE,
}