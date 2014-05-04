package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Data object for a location. References multiple WifiMorsels that have been detected at this location.
 */
public class Location implements Data {
    private int coordinateX, coordinateY;
    private String key;
    /**
     * Contains WifiMorsel
     */
    private DataList<WifiMorsel> wifiMorsels;

    private Location() {
        key = null;
    }

    public Location(int coordinateX, int coordinateY) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        makeKey();
    }

    public Location(int coordinateX, int coordinateY, DataList<WifiMorsel> wifiMorsels) {
        this(coordinateX, coordinateY);
        this.wifiMorsels = wifiMorsels;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
        makeKey();
    }

    private void makeKey() {
        if (getCoordinateX() == 0 && getCoordinateY() == 0) {
            key = null;
        } else
            key = getCoordinateX() + "/" + getCoordinateY();
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
        makeKey();
    }

    public DataList<WifiMorsel> getWifiMorsels() {
        return wifiMorsels;
    }

    public void setWifiMorsels(DataList<WifiMorsel> wifiMorsels) {
        this.wifiMorsels = wifiMorsels;
    }

    @Override
    public String toString() {
        return "Location{" +
                "coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                ", morsels=" + wifiMorsels +
                ", key=" + key +
                '}';
    }

    @Override
    public String getKey() {
        return key;
    }
}
