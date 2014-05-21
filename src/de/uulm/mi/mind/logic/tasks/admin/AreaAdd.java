package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.LocationTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AreaAdd extends LocationTask<Area, Information> {
    @Override
    public Information doWork(Active active, Area area) {
        //Adding locations via area_add is not allowed
        if (area.getLocations() != null) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Adding locations via an area is illegal!");
        }
        if (!safeString(area.getKey())) {
            return new Error(Error.Type.WRONG_OBJECT, "Area to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.create(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was created successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Creation of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Creation of area resulted in an error: The mapping could not be updated.");
        }
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
