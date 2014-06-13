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
public class AreaUpdate extends LocationTask<Area, Information> {
    @Override
    public boolean validateInput(Area object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, final Area area, boolean compact) {
        if (area.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be updated was null!");
        }

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {

                boolean success1 = session.update(area);
                if (success1) {
                    return new Success("Area was updated successfully.");
                } else {
                    return new Error(Error.Type.DATABASE, "Update of area resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "area_update";
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
