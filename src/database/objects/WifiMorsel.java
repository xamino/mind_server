package database.objects;

/**
 * @author Tamino Hartmann
 * Data object for a single wifi data morsel on a location.
 */
public class WifiMorsel {
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
}
