package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.LocationTask;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class AreaUpdate extends LocationTask<Area, Information> {
    @Override
    public Information doWork(Active active, Area area) {
        if (area.getKey() == null) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.WRONG_OBJECT, "Area to be updated was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.update(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was updated successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new Error(Error.Type.DATABASE, "Update of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Update of area resulted in an error: The mapping could not be updated.");
        }
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
