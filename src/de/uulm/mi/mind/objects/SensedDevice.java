package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * @author Tamino Hartmann
 */
public class SensedDevice implements Sendable {

    private String sensor;
    private String position;
    private String ipAddress;
    private int levelValue;

    private SensedDevice() {
    }

    public SensedDevice(String sensor, String ipAddress, int levelValue) {
        this.sensor = sensor;
        this.ipAddress = ipAddress;
        this.levelValue = levelValue;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSensor() {
        return sensor;
    }

    public int getLevelValue() {
        return levelValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
