package database.objects;

import database.Data;

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
    private DataList locations;

    public Area(String ID, DataList locations) {
        this.ID = ID;
        this.locations = locations;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public DataList getLocations() {
        return locations;
    }

    public void setLocations(DataList locations) {
        this.locations = locations;
    }
}
