package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.Task;
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
            return new Error(Error.Type.WRONG_OBJECT, "The Location Module requires a Area, WifiMorsel or Location Object.");
        }

        if (task instanceof Task.Location) {
            if (!(request instanceof Location)) {
                return new Error(Error.Type.TASK, "Location tasks always require a location object!");
            }
            Location location = (Location) request;

            Task.Location locationTask = (Task.Location) task;
            Data retData;
            switch (locationTask) {
                case CREATE:
                    // If a location already exists we simply update it
                    Data read = read(new Location(location.getCoordinateX(), location.getCoordinateY()));
                    if (!(read instanceof DataList)) {
                        // This means something went wrong
                        log.error(TAG, "Failed create: database read returned an error!");
                        return new Error(Error.Type.DATABASE, "Failed create because database read failed!");
                    } else if (((DataList) read).isEmpty()) {
                        // this probably means that no location was found for the given location
                        retData = create(location);
                        // area has changed, so redo mapping
                        updateMapping();
                        return retData;
                    } else {
                        // If a location already exists, we simply add the wifimorsels of the given one
                        Location exist = (Location) ((DataList) read).get(0);
                        exist.getWifiMorsels().addAll(location.getWifiMorsels());
                        retData = update(exist);
                        // area has changed, so redo mapping
                        updateMapping();
                        return retData;
                    }
                case READ:
                    return read(location);
                case UPDATE:
                    retData = update(location);
                    // area has changed, so redo mapping
                    updateMapping();
                    return retData;
                case DELETE:
                    retData = delete(location);
                    // area has changed, so redo mapping
                    updateMapping();
                    return retData;
                case SMALLEST_AREA_BY_LOCATION:
                    return areaByLocation(location);
                case READ_MORSELS:
                    return readMorsels(location);
            }
        } else if (task instanceof Task.Area) {
            // No check because we don't always need it
            Area area = (Area) request;

            Task.Area areaTask = (Task.Area) task;
            switch (areaTask) {
                case CREATE:
                    if (area == null) {
                        return new Error(Error.Type.WRONG_OBJECT, "Area was null!");
                    }
                    Data returnArea = create(area);
                    // area has changed, so redo mapping
                    updateMapping();
                    return returnArea;
                case READ:
                    if (area == null) {
                        return new Error(Error.Type.WRONG_OBJECT, "Filter area was null!");
                    }
                    return read(area);
                case UPDATE:
                    if (area == null) {
                        return new Error(Error.Type.WRONG_OBJECT, "Area was null!");
                    }
                    Data ret2Area = update(area);
                    // area has changed, so redo mapping
                    updateMapping();
                    return ret2Area;
                case DELETE:
                    if (area == null) {
                        return new Error(Error.Type.WRONG_OBJECT, "Filter area was null!");
                    }
                    Data ret3Area = delete(area);
                    // area has changed, so redo mapping
                    updateMapping();
                    return ret3Area;
                case READ_LOCATIONS:
                    if (area == null) {
                        return new Error(Error.Type.WRONG_OBJECT, "Area for locations was null!");
                    }
                    // todo why not read(area).getLocations() ?
                    return readLocations(area);
                case ANNIHILATE:
                    return annihilateAreas();
            }
        }

        return new Error(Error.Type.TASK, "The Location Module is unable to perform the Task as it appears not to be implemented.");
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
        //TODO clone for bug fix WTF
        Area f = new Area(finalArea.getID());
        f.setHeight(finalArea.getHeight());
        f.setWidth(finalArea.getWidth());
        f.setTopLeftX(finalArea.getTopLeftX());
        f.setTopLeftY(finalArea.getTopLeftY());
        f.setLocations(finalArea.getLocations());

        return f;
    }

    /**
     * Method that updates the Location <--> Area mapping.
     */
    private void updateMapping() {
        DataList<Location> locations = database.read(new Location(0, 0, null));
        DataList<Area> areas = database.read(new Area(null));
        log.pushTimer(this, "");
        for (Area area : areas) {
            area.setLocations(new DataList<Location>());
            for (Location location : locations) {
                if (area.contains(location.getCoordinateX(), location.getCoordinateY())) {
                    area.addLocation(location);
                }
            }
            // must write data back to DB
            if (!database.update(area)) {
                log.error(TAG, "Failed to update mapping in DB for " + area.getID() + "!");
            }
        }
        log.log(TAG, "Updated Location <--> Area mapping. Took " + log.popTimer(this).time + "ms.");
    }

    /**
     * Removes all location specific information from the database then restores the default area.
     *
     * @return Success or Error depending on DB action.
     */
    private Data annihilateAreas() {
        Boolean deleted = database.deleteAll(new Area(null, null, 0, 0, 0, 0));
        // Delete these to be sure...
        deleted &= database.deleteAll(new Location(0, 0, null));
        deleted &= database.deleteAll(new WifiMorsel(null, null, 0));
        if (deleted) {
            database.reinit();
            return new Success("All areas were removed from Database.");
        }
        return new Error(Error.Type.DATABASE, "Removal of areas failed.");
    }

    private Data readLocations(Area area) {
        Data data = database.readChildren(area);
        if (data != null)
            return data;
        else
            return new Error(Error.Type.DATABASE, "Reading of " + area.toString() + " Locations failed!");
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
            return new Error(Error.Type.DATABASE, "Reading of " + loc.toString() + " Morsels failed!");
    }
}
