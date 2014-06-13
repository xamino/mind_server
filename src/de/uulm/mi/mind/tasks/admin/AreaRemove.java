package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.LocationTask;

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

                if (success1) {
                    return new Success("Area was deleted successfully.");
                } else {
                    return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error.");
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
