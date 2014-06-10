package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * @author Tamino Hartmann
 *         Data object for a collection of Locations that are grouped into an area, for example a specific room.
 */
public class Area implements Sendable, Saveable {
    /**
     * The identifying value for this area.
     */
    private String ID;
    /**
     * All locations that are within this area.
     */
    private DataList<Location> locations;

    /**
     * Position
     */
    private int topLeftX, topLeftY;
    private int width, height;

    private Area() {
    }

    public Area(String ID) {
        this.ID = ID;
    }

    public Area(String ID, DataList<Location> locations, int topLeftX, int topLeftY, int width, int height) {
        this(ID);
        this.locations = locations;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns area of area. Upper bound is Integer.MAX_VALUE!
     *
     * @return The integer value in m²
     */
    public long getArea() {
        long area = (long) width * (long) height;
        if (area > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) area;
        }
    }

    /**
     * Adds a location to the area.
     *
     * @param location
     */
    public void addLocation(Location location) {
        locations.add(location);
    }

    public int getTopLeftX() {

        return topLeftX;
    }

    public void setTopLeftX(int topLeftX) {
        this.topLeftX = topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(int topLeftY) {
        this.topLeftY = topLeftY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public DataList<Location> getLocations() {
        return locations;
    }

    public void setLocations(DataList<Location> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "Area{" +
                "ID='" + ID + '\'' +
                ", topLeftX=" + topLeftX +
                ", topLeftY=" + topLeftY +
                ", width=" + width +
                ", height=" + height +
                ", locations=" + locations +
                '}';
    }

    /**
     * Collision function for locations.
     *
     * @param X X-coordinate to check.
     * @param Y Y-coordinate to check.
     * @return True if the coorindate is within the area.
     */
    public boolean contains(final double X, final double Y) {
        return (X >= topLeftX && X <= topLeftX + width) && (Y >= topLeftY && Y <= topLeftY + height);
    }

    @Override
    public String getKey() {
        return ID;
    }

    @Override
    public Saveable deepClone() {
        Area a = new Area(ID, null, topLeftX, topLeftY, width, height);

        DataList<Location> locs = new DataList<>();

        if(locations!=null) {
            for (Location location : locations) {
                if (location == null) continue;
                locs.add((Location) location.deepClone());
            }
            a.setLocations(locs);
        }
        else{
            a.setLocations(new DataList<Location>());
        }
        return a;
    }
}
