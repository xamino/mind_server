package database.objects;

import database.Data;

/**
 * @author Tamino Hartmann
 *         Data object for a location. References multiple WifiMorsels that have been detected at this location.
 */
public class Location implements Data {
    private double coordinateX, coordinateY;
    /**
     * Contains WifiMorsel
     */
    private DataList wifiMorsels;

    public Location(double coordinateX, double coordinateY, DataList wifiMorsels) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.wifiMorsels = wifiMorsels;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public DataList getWifiMorsels() {
        return wifiMorsels;
    }

    public void setWifiMorsels(DataList wifiMorsels) {
        this.wifiMorsels = wifiMorsels;
    }

    @Override
    public String toString() {
        return "Location{" +
                "coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY + ", morsels=" + wifiMorsels +
                '}';
    }
}
