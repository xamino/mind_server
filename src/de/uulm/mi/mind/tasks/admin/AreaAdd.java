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
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AreaAdd extends LocationTask<Area, Information> {
    @Override
    public boolean validateInput(Area object) {
        return safeString(object.getID());
    }

    @Override
    public Information doWork(Active active, final Area area, boolean compact) {
        //Adding locations via area_add is not allowed
        if (area.getLocations() != null) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Adding locations via an area is illegal!");
        }
        if (!safeString(area.getKey())) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be created was null!");
        }

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success1 = session.create(area);
                if (success1) {
                    return new Success("Area was created successfully.");
                }
                // Evaluate Error
                else {
                    return new Error(Error.Type.DATABASE, "Creation of area resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "area_add";
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
