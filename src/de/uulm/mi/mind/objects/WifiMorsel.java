package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Data object for a single wifi data morsel on a location.
 */
public class WifiMorsel implements Data, Comparable {
    /**
     * MAC-Address of the wifi access point.
     */
    private String wifiMac;
    /**
     * Name of the wifi access point.
     */
    private String wifiName;
    /**
     * The noise level of the wifi.
     */
    private int wifiLevel;

    public WifiMorsel(String wifiMac, String wifiName, int wifiLevel) {
        this.wifiMac = wifiMac;
        this.wifiName = wifiName;
        this.wifiLevel = wifiLevel;
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiMorsel that = (WifiMorsel) o;

        if (!wifiMac.equals(that.wifiMac)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return wifiMac.hashCode();
    }

    @Override
    public int compareTo(Object obj) {
        WifiMorsel temp = (WifiMorsel) obj;
        if (this.wifiLevel < temp.wifiLevel)
            return -1;
        else if (this.wifiLevel > temp.wifiLevel)
            return 1;
        else
            return 0;
    }

    @Override
    public String getKey() {
        return null;
    }
}
