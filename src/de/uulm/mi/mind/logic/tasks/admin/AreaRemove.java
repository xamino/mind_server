package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.LocationTask;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class AreaRemove extends LocationTask<Area, Information> {
    @Override
    public boolean validateInput(Area object) {
        return true;
    }

    @Override
    public Information doWork(Active active, final Area area, boolean compact) {
        // key is allowed to be null here to delete all
        return (Information) database.open(new Transaction() {
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

    @Override
    public String getTaskName() {
        return "area_remove";
    }

    @Override
    public Class<Area> getInputType() {
        return Area.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
