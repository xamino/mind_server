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
public class AreaRemove extends LocationTask<Area, Information> {
    @Override
    public Information doWork(Active active, Area area) {
        // key is allowed to be null here to delete all
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success1 = database.delete(sessionContainer, area);
        boolean success2 = updateMapping(sessionContainer);

        if (success1 && success2) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("Area was deleted successfully.");
        }

        // some kind of error occurred
        sessionContainer.rollback();
        sessionContainer.close();

        // Evaluate Error
        if (!success1) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Deletion of area resulted in an error.");
        } else { //!success2
            return new Error(Error.Type.DATABASE, "Deletion of area resulted in an error: The mapping could not be updated.");
        }
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
