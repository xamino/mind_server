package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.LocationTask;

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

                if (success1) {
                    return new Success("Location was deleted successfully.");
                }
                // Evaluate Error
                else {
                    return new Error(Error.Type.DATABASE, "Deletion of location resulted in an error.");
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
