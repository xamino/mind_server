package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * @author Tamino Hartmann
 *         Data object for a location. References multiple WifiMorsels that have been detected at this location.
 */
public class Location implements Sendable, Saveable {
    private int coordinateX, coordinateY;
    private String key;
    /**
     * Contains WifiMorsel
     */
    private DataList<WifiMorsel> wifiMorsels;
    private String unique;

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

    @Override
    public Saveable deepClone() {
        Location loc = new Location(coordinateX, coordinateY);
        loc.setUnique(unique);

        if (wifiMorsels != null) {
            DataList<WifiMorsel> morsels = new DataList<>();
            for (WifiMorsel wifiMorsel : wifiMorsels) {
                if (wifiMorsel == null) continue;
                morsels.add((WifiMorsel) wifiMorsel.deepClone());
            }
            loc.setWifiMorsels(morsels);
        } else {
            loc.setWifiMorsels(new DataList<WifiMorsel>());
        }
        return loc;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }
}
