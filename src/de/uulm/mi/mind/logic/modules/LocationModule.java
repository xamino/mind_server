package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.io.DatabaseManager;
import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

import java.util.ArrayList;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Auch genannt LoMo – wie slowmo, aber besser.
 */
public class LocationModule implements Module {

    private final DatabaseManager database;
    private final Messenger log;
    private final String TAG = "LocationModule";
    private ArrayList<String> wifiNameFilter;

    public LocationModule() {
        wifiNameFilter = Configuration.getInstance().getWifiNameFilter();
        database = DatabaseManager.getInstance();
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

    private Data createArea(final Area area) {
        if (area.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be created was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success1 = session.create(area);
                boolean success2 = updateMapping(session);

                if (success1 && success2) {
                    return new Success("Area was created successfully.");
                }

                // Evaluate Error
                if (!success1) {
                    return new Error(Error.Type.DATABASE, "Creation of area resulted in an error.");
                } else { //!success2
                    return new Error(Error.Type.DATABASE, "Creation of area resulted in an error: The mapping could not be updated.");
                }
            }
        });
    }

    private Data readArea(final Area area) {
        DataList<Area> read = (DataList<Area>) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                return session.read(area);
            }
        });

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

    private Data updateArea(final Area area) {
        if (area.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be updated was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {

                boolean success1 = session.update(area);
                boolean success2 = updateMapping(session);

                if (success1 && success2) {
                    return new Success("Area was updated successfully.");
                }

                // Evaluate Error
                if (!success1) {
                    return new Error(Error.Type.DATABASE, "Update of area resulted in an error.");
                } else { //!success2
                    return new Error(Error.Type.DATABASE, "Update of area resulted in an error: The mapping could not be updated.");
                }
            }
        });


    }

    private Data deleteArea(final Area area) {
        // key is allowed to be null here to delete all
        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success1 = session.delete(area);
                boolean success2 = updateMapping(session);

                if (success1 && success2) {
                    return new Success("Area was deleted successfully.");
                }

                // some kind of error occurred
                // Evaluate Error
                if (!success1) {
                    return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error.");
                } else { //!success2
                    return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error: The mapping could not be updated.");
                }
            }
        });

    }

    private Data createLocation(final Location location) {
        if (location.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Location to be created was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // If a location already exists we simply update it
                DataList<Location> read = session.read(new Location(location.getCoordinateX(), location.getCoordinateY()));
                if (read.isEmpty()) {
                    // this probably means that no location was found for the given location
                    // so filter
                    Location filteredLocation = filterMorsels(location);
                    // and create
                    boolean success1 = session.create(filteredLocation);
                    // area has changed, so redo mapping
                    boolean success2 = updateMapping(session);

                    if (success1 && success2) {
                        return new Success("Location was created successfully.");
                    }

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
                    // filter
                    exist = filterMorsels(exist);
                    boolean success1 = session.update(exist);
                    // area has changed, so redo mapping
                    boolean success2 = updateMapping(session);

                    if (success1 && success2) {
                        return new Success("Location was not created but updated successfully as it existed already.");
                    }

                    // Evaluate Error
                    if (!success1) {
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error.");
                    } else { //!success2
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error: The mapping could not be updated.");
                    }
                }
            }
        });
    }

    private Data readLocation(final Location location) {
        DataList<Location> read = (DataList<Location>) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                return session.read(location);
            }
        });

        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of location resulted in an error.");
        }

        // get filtered locations
        if (location.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "Location could not be found!");
        }
        return read;
    }

    private Data updateLocation(final Location location) {
        if (location.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Location to be updated was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // filter
                Location filteredLocation = filterMorsels(location);
                // run all operations
                boolean success1 = session.update(filteredLocation);
                // area has changed, so redo mapping
                boolean success2 = updateMapping(session);

                if (success1 && success2) {
                    return new Success("Location was updated successfully.");
                }

                // Evaluate Error
                if (!success1) {
                    return new Error(Error.Type.DATABASE, "Update of location resulted in an error.");
                } else { //!success2
                    return new Error(Error.Type.DATABASE, "Update of location resulted in an error: The mapping could not be updated.");
                }
            }
        });
    }

    private Data deleteLocation(final Location location) {
        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // run all operations
                boolean success1 = session.delete(location);
                // area has changed, so redo mapping
                boolean success2 = updateMapping(session);

                if (success1 && success2) {
                    return new Success("Location was deleted successfully.");
                }

                // Evaluate Error
                if (!success1) {
                    return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error.");
                } else { //!success2
                    return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error: The mapping could not be updated.");
                }
            }
        });
    }

    /**
     * Method that returns the smallest area that contains the given location.
     *
     * @param location
     * @return
     */
    private Area readAreaByLocation(final Location location) {
        return (Area) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {

                // Get all areas
                DataList<Area> dbCall = session.read(new Area(null));
                if (dbCall == null) {
                    log.error(TAG, "All areas: dbCall == null – shouldn't happen, FIX!");
                    return null;
                }
                DataList<Area> all = dbCall;
                dbCall = session.read(new Area("University"));
                if (dbCall == null) {
                    log.error(TAG, "University: dbCall == null – shouldn't happen, FIX!");
                    return null;
                }

                Area finalArea = dbCall.get(0);
                for (Area temp : all) {
                    if (temp.getArea() < finalArea.getArea()
                            && temp.contains(location.getCoordinateX(), location.getCoordinateY())) {
                        finalArea = temp;
                    }
                }

                return finalArea;
            }
        });
    }

    /**
     * Method that updates the Location <--> Area mapping.
     */
    private boolean updateMapping(Session session) {
        DataList<Location> locations = session.read(new Location(0, 0, null));
        DataList<Area> areas = session.read(new Area(null));

        log.pushTimer(this, "");
        for (Area area : areas) {
            area.setLocations(new DataList<Location>());
            for (Location location : locations) {
                if (area.contains(location.getCoordinateX(), location.getCoordinateY())) {
                    area.addLocation(location);
                }
            }
            // must write data back to DB
            if (!session.update(area)) {
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
        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                Boolean deleted = session.delete(new Area(null));
                // Delete these to be sure...
                deleted &= session.delete(new Location(0, 0, null));
                deleted &= session.delete(new WifiMorsel(null, null, 0, 0, null,null));
                if (deleted) {
                    session.reinit();
                    return new Success("All areas were removed from Database.");
                }
                return new Error(Error.Type.DATABASE, "Removal of areas failed.");
            }
        });
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

    /**
     * Function that filters the WifiMorsels of a Location against the given list to filter out unwanted wifi hotspots
     * (for example temporary ones or ones we don't want messing with the algorithm).
     *
     * @param location The Location object to filter.
     * @return The location object with the filtered morsels.
     */
    private Location filterMorsels(Location location) {
        DataList<WifiMorsel> morsels = new DataList<>();
        for (WifiMorsel morsel : location.getWifiMorsels()) {
            for (String s : wifiNameFilter) {
                String[] nameChannel = s.split("\\|");
                String name = nameChannel[0];

                boolean sameChannel = true;
                if (nameChannel.length > 1) {
                    String channel = nameChannel[1];
                    if (!channel.equals("*") && !channel.equals(String.valueOf(morsel.getWifiChannel()))) {
                        sameChannel = false;
                    }
                }
                if (name.equals(morsel.getWifiName()) && sameChannel) {
                    morsels.add(morsel);
                }
            }
        }
        location.setWifiMorsels(morsels);
        return location;
    }
}
