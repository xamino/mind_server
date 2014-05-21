package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class DisplayRemove extends AdminTask<PublicDisplay, Information> {
    @Override
    public Information doWork(Active active, PublicDisplay display) {
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.delete(sessionContainer, display);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            if (display.getKey() == null) {
                return new Success("All PublicDisplay were deleted successfully.");
            }
            return new Success("PublicDisplay was deleted successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Deletion of PublicDisplay resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "display_remove";
    }

    @Override
    public Class<PublicDisplay> getInputType() {
        return PublicDisplay.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
