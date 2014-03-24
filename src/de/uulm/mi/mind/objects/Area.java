package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Data object for a collection of Locations that are grouped into an area, for example a specific room.
 */
public class Area implements Data {
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

    public Area(String ID, DataList<Location> locations, int topLeftX, int topLeftY, int width, int height) {
        this.ID = ID;
        this.locations = locations;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns area of area.
     *
     * @return The integer value in m²
     */
    public int getArea() {
        return width * height;
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
                ", locations='" + locations +
                "'}";
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
}
