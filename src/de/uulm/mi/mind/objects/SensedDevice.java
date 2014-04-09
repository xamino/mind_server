package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 */
public class SensedDevice implements Data {

    private String ipAddress;

    public SensedDevice(String ipAddress, int levelValue) {
        this.ipAddress = ipAddress;
        this.levelValue = levelValue;
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

    private int levelValue;

    @Override
    public String getKey() {
        return null;
    }
}
