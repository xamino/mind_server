package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Success;
import database.messages.Error;
import database.objects.Area;
import database.objects.Location;
import database.objects.WifiMorsel;
import logic.Module;
import logic.Task;

/**
 * @author Tamino Hartmann
 *
 * Auch genannt LoMo – wie slowmo, aber besser.
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
                    return readLocation(location);
                case UPDATE:
                    return updateLocation(location);
                case DELETE:
                    return deleteLocation(location);
                case READ_MORSELS:
                    return readMorsels(location);
            }
        } else if (request instanceof WifiMorsel) {
        } else if (request instanceof Area) {
        } else {
        }

        return null;
    }

    private Data readMorsels(Location loc) {
        Data data = database.read(loc);
        if (data != null)
            return data;
        else
            return new Error("LocationMorselReadFailure", "Reading of " + loc.toString() + " Morsels failed!");
    }

    private Data deleteLocation(Location loc) {
        boolean op = database.delete(loc);
        if (op)
            return new Success("LocationDeletionSuccess", "The " + loc.toString() + " was deleted successfully.");
        else
            return new Error("LocationDeletionFailure", "Deletion of " + loc.toString() + " failed");
    }

    private Data updateLocation(Location loc) {
        boolean op = database.update(loc);
        if (op)
            return new Success("LocationUpdateSuccess", "The " + loc.toString() + " was updated successfully.");
        else
            return new Error("LocationUpdateFailure", "Update " + loc.toString() + " failed!");
    }

    private Data readLocation(Location loc) {
        Data data = database.read(loc);
        if (data != null)
            return data;
        else
            return new Error("LocationReadFailure", "Reading of " + loc.toString() + " failed!");
    }

    private Data createLocation(Location loc) {
        boolean op = database.create(loc);
        if (op)
            return new Success("LocationCreationSuccess", "The " + loc.toString() + " was created successfully.");
        else
            return new Error("LocationCreationFailure", "Creation of " + loc.toString() + " failed!");
    }
}
