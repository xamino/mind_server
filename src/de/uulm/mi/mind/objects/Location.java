package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Data object for a location. References multiple WifiMorsels that have been detected at this location.
 */
public class Location implements Data {
    private int coordinateX, coordinateY;
    /**
     * Contains WifiMorsel
     */
    private DataList<WifiMorsel> wifiMorsels;

    public Location(int coordinateX, int coordinateY) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
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
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
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
                ", coordinateY=" + coordinateY + ", morsels=" + wifiMorsels +
                '}';
    }

    @Override
    public String getKey() {
        return String.valueOf(getCoordinateX()) + "/" + String.valueOf(getCoordinateY());
    }
}
