package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.tasks.LocationTask;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.WifiMorsel;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class LocationAdd extends LocationTask {

    @Override
    public Information doWork(Active active, Location location) {
        if (location.getKey() == null) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.WRONG_OBJECT, "Location to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();
        // If a location already exists we simply update it
        DataList<Location> read = database.read(sessionContainer, new Location(location.getCoordinateX(), location.getCoordinateY()));
        if (read.isEmpty()) {
            // this probably means that no location was found for the given location
            // so filter
            location = filterMorsels(location);
            // and create
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
            // filter
            exist = filterMorsels(exist);
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

    @Override
    public String getTaskName() {
        return "location_add";
    }
}
