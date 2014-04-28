package de.uulm.mi.mind.logic.modules;

import com.db4o.ObjectContainer;
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
public class LocationModule implements Module {

    private final DatabaseController database;
    private final Messenger log;
    private final String TAG = "LocationModule";

    public LocationModule() {
        database = DatabaseController.getInstance();
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {

        if (task == Task.Area.ANNIHILATE) {
            return annihilateAreas();
        }

        // Location
        else if (task instanceof Task.Location) {
            if (request == null) {
                request = new Location(0, 0, null);
            } else if (!(request instanceof Location)) {
                return new Error(Error.Type.WRONG_OBJECT, "Location tasks always require a location object!");
            }
            Location location = (Location) request;

            Task.Location locationTask = (Task.Location) task;
            switch (locationTask) {
                case CREATE:
                    return createLocation(location);
                case READ:
                    return readLocation(location);
                case UPDATE:
                    return updateLocation(location);
                case DELETE:
                    return deleteLocation(location);
                case SMALLEST_AREA_BY_LOCATION:
                    return readAreaByLocation(location);
                case READ_MORSELS:
                    return readMorsels(location);
            }
            // Area
        } else if (task instanceof Task.Area) {
            if (request == null) {
                request = new Area(null);
            } else if (!(request instanceof Area)) {
                return new Error(Error.Type.WRONG_OBJECT, "Area tasks always require an area object!");
            }
            Area area = (Area) request;

            Task.Area areaTask = (Task.Area) task;
            switch (areaTask) {
                case CREATE:
                    return createArea(area);
                case READ:
                    return readArea(area);
                case UPDATE:
                    return updateArea(area);
                case DELETE:
                    return deleteArea(area);
                case READ_LOCATIONS:
                    return readLocations(area);
            }
        }

        return new Error(Error.Type.TASK, "The Location Module is unable to perform the Task as it appears not to be implemented.");
    }

    private Data createArea(Area area) {
        if (area.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.create(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was created successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Creation of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Creation of area resulted in an error: The mapping could not be updated.");
        }
    }

    private Data readArea(Area area) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<Area> read = database.read(sessionContainer, area);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of area resulted in an error.");
        }

        // get filtered Areas
        if (area.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "Area could not be found!");
        }
        return read;
    }

    private Data updateArea(Area area) {
        if (area.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be updated was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.update(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was updated successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Update of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Update of area resulted in an error: The mapping could not be updated.");
        }
    }

    private Data deleteArea(Area area) {
        // key is allowed to be null here to delete all
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.delete(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was deleted successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error: The mapping could not be updated.");
        }
    }

    private Data createLocation(Location location) {
        if (location.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Location to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();
        // If a location already exists we simply update it
        DataList<Location> read = database.read(sessionContainer, new Location(location.getCoordinateX(), location.getCoordinateY()));
        if (read.isEmpty()) {
            // this probably means that no location was found for the given location
            boolean success1 = database.create(sessionContainer, location);
            // area has changed, so redo mapping
            boolean success2 = updateMapping(sessionContainer);

            if (success1 && success2) {
                sessionContainer.commit();
                sessionContainer.close();
                return new Success("Location was created successfully.");
            }

            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();

            // Evaluate Error
            if (!success1) {
                return new Error(Error.Type.DATABASE, "Creation of location resulted in an error.");
            } else { //!success2
                return new Error(Error.Type.DATABASE, "Creation of location resulted in an error: The mapping could not be updated.");
            }

        } else {
            // If a location already exists, we simply add the wifimorsels of the given one
            Location exist = read.get(0);
            exist.getWifiMorsels().addAll(location.getWifiMorsels());
            boolean success1 = database.update(sessionContainer, exist);
            // area has changed, so redo mapping
            boolean success2 = updateMapping(sessionContainer);

            if (success1 && success2) {
                sessionContainer.commit();
                sessionContainer.close();
                return new Success("Location was not created but updated successfully as it existed already.");
            }

            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();

            // Evaluate Error
            if (!success1) {
                return new Error(Error.Type.DATABASE, "Creation of location resulted in an error.");
            } else { //!success2
                return new Error(Error.Type.DATABASE, "Creation of location resulted in an error: The mapping could not be updated.");
            }
        }
    }

    private Data readLocation(Location location) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<Location> read = database.read(sessionContainer, location);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of location resulted in an error.");
        }

        // get filtered locations
        if (location.getKey() == null || location.getKey().equals("0/0")) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "Location could not be found!");
        }
        return read;


    }

    private Data updateLocation(Location location) {
        if (location.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Location to be updated was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        // run all operations
        boolean success1 = database.update(sessionContainer, location);
        // area has changed, so redo mapping
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Location was updated successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Update of location resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Update of location resulted in an error: The mapping could not be updated.");
        }
    }

    private Data deleteLocation(Location location) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        // run all operations
        boolean success1 = database.delete(sessionContainer, location);
        // area has changed, so redo mapping
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Location was deleted successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error: The mapping could not be updated.");
        }
    }

    /**
     * Method that returns the smallest area that contains the given location.
     *
     * @param location
     * @return
     */
    private Area readAreaByLocation(final Location location) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        // Get all areas
        DataList<Area> dbCall = database.read(sessionContainer, new Area(null));
        if (dbCall == null) {
            log.error(TAG, "All areas: dbCall == null – shouldn't happen, FIX!");
            return null;
        }
        DataList<Area> all = dbCall;
        dbCall = database.read(sessionContainer, new Area("University"));
        if (dbCall == null) {
            log.error(TAG, "University: dbCall == null – shouldn't happen, FIX!");
            return null;
        }

        sessionContainer.close();

        Area finalArea = dbCall.get(0);
        for (Area temp : all) {
            if (temp.getArea() < finalArea.getArea()
                    && temp.contains(location.getCoordinateX(), location.getCoordinateY())) {
                finalArea = temp;
            }
        }

        return finalArea;
    }

    /**
     * Method that updates the Location <--> Area mapping.
     *
     * @param sessionContainer
     */
    private boolean updateMapping(ObjectContainer sessionContainer) {

        DataList<Location> locations = database.read(sessionContainer, new Location(0, 0, null));
        DataList<Area> areas = database.read(sessionContainer, new Area(null));

        log.pushTimer(this, "");
        for (Area area : areas) {
            area.setLocations(new DataList<Location>());
            for (Location location : locations) {
                if (area.contains(location.getCoordinateX(), location.getCoordinateY())) {
                    area.addLocation(location);
                }
            }
            // must write data back to DB
            if (!database.update(sessionContainer, area)) {
                log.error(TAG, "Failed to update mapping in DB for " + area.getID() + "!");
                return false;
            }
        }

        log.log(TAG, "Updated Location <--> Area mapping. Took " + log.popTimer(this).time + "ms.");
        return true;
    }

    /**
     * Removes all location specific information from the database then restores the default area.
     *
     * @return Success or Error depending on DB action.
     */
    private Data annihilateAreas() {
        ObjectContainer sessionContainer = database.getSessionContainer();
        Boolean deleted = database.delete(sessionContainer, new Area(null));
        // Delete these to be sure...
        deleted &= database.delete(sessionContainer, new Location(0, 0, null));
        deleted &= database.delete(sessionContainer, new WifiMorsel(null, null, 0, 0));
        if (deleted) {
            database.reinit(sessionContainer);
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("All areas were removed from Database.");
        }
        sessionContainer.rollback();
        sessionContainer.close();
        return new Error(Error.Type.DATABASE, "Removal of areas failed.");
    }

    private Data readLocations(Area area) {
        if (area == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area for locations was null!");
        }

        Data read = readArea(area);
        if (read instanceof Error) {
            return read;
        }

        if (read instanceof DataList && !((DataList<Area>) read).isEmpty()) {
            return ((DataList<Area>) read).get(0).getLocations();
        } else
            return new Error(Error.Type.DATABASE, "Reading of " + area + " Locations failed!");
    }


    /**
     * Returns available WifiMorsels for a specified Location
     *
     * @param loc The Location of the WifiMorsels to be returned
     * @return WifiMorsels at the Location specified as parameter
     */
    private Data readMorsels(Location loc) {
        if (loc == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Location for morsels was null!");
        }

        Data read = readLocation(loc);
        if (read instanceof Error) {
            return read;
        }

        if (read instanceof DataList && !((DataList<Location>) read).isEmpty()) {
            return ((DataList<Location>) read).get(0).getWifiMorsels();
        } else
            return new Error(Error.Type.DATABASE, "Reading of " + loc + " Morsels failed!");
    }
}
