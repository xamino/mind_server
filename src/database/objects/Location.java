package database.objects;

import database.Data;

import java.util.ArrayList;

/**
 * @author Tamino Hartmann
 *         Data object for a location. References multiple WifiMorsels that have been detected at this location.
 */
public class Location implements Data {
    private double coordinateX, coordinateY;
    private ArrayList wifiNetworks;

    public Location() {

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

    public ArrayList getWifiNetworks() {
        return wifiNetworks;
    }

    public void setWifiNetworks(ArrayList wifiNetworks) {
        this.wifiNetworks = wifiNetworks;
    }
}
