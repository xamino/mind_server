package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.tasks.LocationTask;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class LocationRead extends LocationTask {
    @Override
    public Sendable doWork(Active active, Location location) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<Location> read = database.read(sessionContainer, location);
        sessionContainer.close();
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

    @Override
    public String getTaskName() {
        return "location_read";
    }
}
