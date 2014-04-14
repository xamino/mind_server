package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 */
public class SensedDevice implements Data {

    private String sensor;

    private String ipAddress;
    private int levelValue;

    public SensedDevice(String sensor, String ipAddress, int levelValue) {
        this.sensor = sensor;
        this.ipAddress = ipAddress;
        this.levelValue = levelValue;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public int getLevelValue() {

        return levelValue;
    }

    public void setLevelValue(int levelValue) {
        this.levelValue = levelValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String getKey() {
        return null;
    }
}
