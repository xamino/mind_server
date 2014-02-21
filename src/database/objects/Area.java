package database.objects;

import database.Data;

import java.util.ArrayList;

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
    private ArrayList locations;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public ArrayList getLocations() {
        return locations;
    }

    public void setLocations(ArrayList locations) {
        this.locations = locations;
    }
}
