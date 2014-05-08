package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * @author Tamino Hartmann
 *         Data object for a single wifi data morsel on a location.
 */
public class WifiMorsel implements Sendable, Comparable, Saveable {
    /**
     * MAC-Address of the wifi access point.
     */
    private String wifiMac;
    /**
     * Name of the wifi access point.
     */
    private String wifiName;
    /**
     * The strength level of the wifi signal.
     */
    private int wifiLevel;

    private int wifiChannel;

    private String deviceModel;

    private WifiMorsel() {
    }

    public WifiMorsel(String wifiMac, String wifiName, int wifiLevel, int wifiChannel, String deviceModel) {
        this.wifiMac = wifiMac;
        this.wifiName = wifiName;
        this.wifiLevel = wifiLevel;
        this.wifiChannel = wifiChannel;
        this.deviceModel = deviceModel;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String wifiMac) {
        this.wifiMac = wifiMac;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public int getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(int wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    @Override
    public String toString() {
        return "WifiMorsel{" +
                "wifiMac='" + wifiMac + '\'' +
                ", wifiName='" + wifiName + '\'' +
                ", wifiLevel=" + wifiLevel +
                ", wifiChannel=" + wifiChannel +
                ", deviceModel='" + deviceModel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiMorsel that = (WifiMorsel) o;

        return wifiMac.equals(that.wifiMac);
    }

    @Override
    public int hashCode() {
        return wifiMac.hashCode();
    }

    @Override
    public int compareTo(Object obj) {
        WifiMorsel temp = (WifiMorsel) obj;
        if (this.wifiLevel > temp.wifiLevel)
            return -1;
        else if (this.wifiLevel < temp.wifiLevel)
            return 1;
        else
            return 0;
    }

    public int getWifiChannel() {
        return wifiChannel;
    }

    public void setWifiChannel(int wifiChannel) {
        this.wifiChannel = wifiChannel;
    }

    @Override
    public String getKey() {
        return null;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
}
