package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Error;
import database.messages.Success;
import database.objects.*;
import logic.Module;
import logic.Task;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Auch genannt LoMo – wie slowmo, aber besser.
 */
public class LocationModule extends Module {

    private final DatabaseController database;

    public LocationModule() {
        database = DatabaseController.getInstance();
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
                case READ_MORSELS:
                    return readMorsels(location);
                //TODO READ_BEST_AREA
                //TODO area add, update -> update locations
            }
        } else if (request instanceof WifiMorsel) {
            //TODO are there any other operations directly on these?
        } else if (request instanceof Area || request == null) {
            Area area = (Area) request;

            Task.Area areaTask = (Task.Area) task;
            switch (areaTask) {
                case CREATE:
                    return create(area);
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

    private Data readArea(Area area) {
        // get all Users
        if (area == null)
            return read(new User(null, null));
        // get filtered Users
        if (area.getID() == null)
            return read(area);

        // from here on single users were requested
        Data data = read(area);
        if (data instanceof DataList)
            return ((DataList<Data>) data).get(0);
        else return data; // Error
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
        DataList<Location> locations = new DataList<>();
        locations.add(location);
        DataList<Area> containedAreas = database.read(new Area(null, locations, 0, 0, 0, 0));

        for (Area area : containedAreas) {
            area.addLocation(location);
            database.update(area);
        }

        return new Success("CreateLocationSuccess", "The Location " + location.toString() + " was stored in the Database.");
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
