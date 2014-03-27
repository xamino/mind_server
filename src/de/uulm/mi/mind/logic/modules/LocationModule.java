package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.logic.Task;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.servlet.ServletFunctions;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Auch genannt LoMo – wie slowmo, aber besser.
 */
public class LocationModule extends Module {

    private final DatabaseController database;
    private final Messenger log;
    private final String TAG = "LocationModule";

    public LocationModule() {
        database = DatabaseController.getInstance();
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (!(request instanceof Area || request instanceof WifiMorsel || request instanceof Location) && request != null) {
            return new Error("WrongDataType", "The Location Module requires a Area, WifiMorsel or Location Object.");
        }

        if (task instanceof Task.Location) {
            if (!(request instanceof Location)) {
                return new Error("IllegalLocationTask", "Location tasks always require a location object!");
            }
            Location location = (Location) request;

            Task.Location locationTask = (Task.Location) task;
            // NOTE: The Area-Location binding is handled solely by the Area functions, so no need to update that here!
            switch (locationTask) {
                case CREATE:
                    return create(location);
                case READ:
                    return read(location);
                case UPDATE:
                    return update(location);
                case DELETE:
                    return delete(location);
                case SMALLEST_AREA_BY_LOCATION:
                    return areaByLocation(location);
                case READ_MORSELS:
                    return readMorsels(location);
            }
        } else if (task instanceof Task.Area) {
            Area area = (Area) request;

            Task.Area areaTask = (Task.Area) task;
            switch (areaTask) {
                case CREATE:
                    if (area == null) {
                        return new Error("IllegalAreaCreate", "Area was null!");
                    }
                    area = updateLocations(area);
                    return create(area);
                case READ:
                    if (area == null) {
                        return new Error("IllegalAreaRead", "Filter area was null!");
                    }
                    // We might need to update area for new locations:
                    Data data = read(area);
                    if (!(data instanceof DataList)) {
                        return new Error("AreaReadUpdateError", "Failed to start location update upon read!");
                    }
                    DataList areas = ((DataList) data);
                    for (Object obj : areas) {
                        Area temp = ((Area) obj);
                        temp = updateLocations(temp);
                        if (null == ServletFunctions.getInstance().checkDataMessage(update(temp), Success.class)) {
                            return new Error("AreaReadUpdateError", "Failed to update " + temp.getID() + " upon read!");
                        }
                    }
                    return areas;
                case UPDATE:
                    if (area == null) {
                        return new Error("IllegalAreaUpdate", "Area was null!");
                    }
                    area = updateLocations(area);
                    return update(area);
                case DELETE:
                    if (area == null) {
                        return new Error("IllegalAreaDelete", "Filter area was null!");
                    }
                    return delete(area);
                case READ_LOCATIONS:
                    if (area == null) {
                        return new Error("IllegalReadLocations", "Area for locations was null!");
                    }
                    return readLocations(area);
                case ANNIHILATE:
                    return annihilateAreas();
            }
        }

        return new Error("MissingOperation", "The Location Module is unable to perform the Task as it appears not to be implemented.");
    }

    /**
     * Method that returns the smallest area that contains the given location.
     *
     * @param location
     * @return
     */
    private Area areaByLocation(final Location location) {
        // Get all areas
        Data dbCall = read(new Area(null, null, 0, 0, 0, 0));
        if (!(dbCall instanceof DataList)) {
            log.error(TAG, "All areas: dbCall != Datalist – shouldn't happen, FIX!");
            log.error(TAG, "dbCall is of type " + dbCall);
            return null;
        }
        DataList all = (DataList) dbCall;
        dbCall = read(new Area("universe", null, 0, 0, 0, 0));
        if (!(dbCall instanceof DataList)) {
            log.error(TAG, "Universe: dbCall != Datalist – shouldn't happen, FIX!");
            log.error(TAG, "dbCall is of type " + dbCall);
            return null;
        }
        Area finalArea = (Area) ((DataList) dbCall).get(0);
        for (Object data : all) {
            Area temp = (Area) data;
            if (temp.getArea() < finalArea.getArea()
                    && temp.contains(location.getCoordinateX(), location.getCoordinateY())) {
                finalArea = temp;
            }
        }
        return finalArea;
    }

    /**
     * Method that updates all Locations that are to be associated with an area.
     *
     * @param area
     * @return
     */
    private Area updateLocations(Area area) {
        DataList<Location> data = database.read(new Location(0, 0, null));
        area.setLocations(new DataList<Location>());
        for (Location loc : data) {
            if (area.contains(loc.getCoordinateX(), loc.getCoordinateY())) {
                area.addLocation(loc);
            }
        }
        return area;
    }

    /**
     * Removes all location specific information from the database then restores the default area.
     *
     * @return
     */
    private Data annihilateAreas() {
        Boolean deleted = database.deleteAll(new Area(null, null, 0, 0, 0, 0));
        // Delete these to be sure...
        deleted &= database.deleteAll(new Location(0, 0, null));
        deleted &= database.deleteAll(new WifiMorsel(null, null, 0));
        if (deleted) {
            database.reinit();
            return new Success("AreaAnnihilationSuccess", "All areas were removed from Database.");
        }
        return new Error("AreaAnnihilationFailure", "Removal of areas failed.");
    }

    private Data readLocations(Area area) {
        Data data = database.readChildren(area);
        if (data != null)
            return data;
        else
            return new Error("AreaLocationReadFailure", "Reading of " + area.toString() + " Locations failed!");
    }


    /**
     * Returns available WifiMorsels for a specified Location
     *
     * @param loc The Location of the WifiMorsels to be returned
     * @return WifiMorsels at the Location specified as parameter
     */
    private Data readMorsels(Location loc) {
        Data data = database.readChildren(loc);
        if (data != null)
            return data;
        else
            return new Error("LocationMorselReadFailure", "Reading of " + loc.toString() + " Morsels failed!");
    }
}
