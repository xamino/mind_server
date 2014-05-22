package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.LocationTask;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class LocationRemove extends LocationTask<Location, Information> {
    @Override
    public Information doWork(Active active, Location location) {
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
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Deletion of location resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error: The mapping could not be updated.");
        }
    }

    @Override
    public String getTaskName() {
        return "location_remove";
    }

    @Override
    public Class<Location> getInputType() {
        return Location.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
