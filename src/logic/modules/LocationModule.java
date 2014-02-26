package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Error;
import database.objects.Area;
import database.objects.Location;
import database.objects.WifiMorsel;
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
                    return create(location);
                case READ:
                    return read(location);
                case UPDATE:
                    return update(location);
                case DELETE:
                    return delete(location);
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
}
