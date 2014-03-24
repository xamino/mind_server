package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.logic.Task;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

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

        if (request instanceof Location) {
            Location location = (Location) request;

            Task.Location locationTask = (Task.Location) task;
            switch (locationTask) {
                case CREATE:
                    return createLocation(location);
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
                //TODO area add, update -> update locations
            }
        } else if (request instanceof WifiMorsel) {
            //TODO are there any other operations directly on these?
        } else if (request instanceof Area || request == null) {
            Area area = (Area) request;

            Task.Area areaTask = (Task.Area) task;
            switch (areaTask) {
                case CREATE:
                    return createArea(area);
                case READ:
                    return readArea(area);
                case UPDATE:
                    return update(area);
                case DELETE:
                    return delete(area);
                case READ_LOCATIONS:
                    return readLocations(area);
                case ANNIHILATE:
                    return annihilateAreas();
            }
        }

        return new Error("MissingOperation", "The Location Module is unable to perform the Task as it appears not to be implemented.");
    }

    private Data createArea(Area area) {
        if (area == null) {
            return new Error("IllegalAddArea", "Area to add was null.");
        }

        // We need to write all locations into area
        DataList<Location> data = database.read(new Location(0, 0, null));
        area.setLocations(new DataList<Location>());
        for (Location loc : data) {
            log.log(TAG, "Testing " + loc.toString() + " against " + area.toString());
            if (area.contains(loc.getCoordinateX(), loc.getCoordinateY())) {
                area.addLocation(loc);
            }
        }
        return create(area);
    }

    /**
     * Method that returns the smallest area that contains the given location.
     *
     * @param location
     * @return
     */
    private Area areaByLocation(Location location) {
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

    private Data readArea(Area area) {
        // get all Areas
        if (area == null) {
            return read(new Area(null, null, 0, 0, 0, 0));
        }
        return read(area);
    }

    /**
     * Adds a location to all relevant areas in the database.
     *
     * @param location The location to add.
     * @return A message signifying status.
     */
    private Data createLocation(final Location location) {
        // TODO
        // Locations must always be in universe! Should always work automatically. Don't forcibly add it in advance,
        // the location will then be added twice! How do we enforce this better?

        // Now read all Areas from DB where the Location coordinates are contained
        DataList<Area> containedAreas = database.getAreasContainingLocation(location);

        boolean success = true;
        for (Area area : containedAreas) {
            area.addLocation(location);
            success &= database.update(area);
        }
        if (success) {
            return new Success("CreateLocationSuccess", "The Location " + location.toString() + " was stored in the Database.");
        } else {
            return new Error("CreateLocationFailure", "The Location " + location.toString() + " could not be stored in the Database.");
        }

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
            database.init();
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
