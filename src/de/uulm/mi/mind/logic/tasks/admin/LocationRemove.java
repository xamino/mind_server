package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.LocationTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class LocationRemove extends LocationTask<Location, Information> {
    @Override
    public boolean validateInput(Location object) {
        return true;
    }

    @Override
    public Information doWork(Active active, final Location location, boolean compact) {
        return (Information) database.open(new Transaction() {
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
