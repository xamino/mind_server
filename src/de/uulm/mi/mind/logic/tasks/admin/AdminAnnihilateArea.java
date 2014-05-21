package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.WifiMorsel;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminAnnihilateArea extends AdminTask<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        log.log(TAG, "Removing all area objects!");
        ObjectContainer sessionContainer = database.getSessionContainer();
        Boolean deleted = database.delete(sessionContainer, new Area(null));
        // Delete these to be sure...
        deleted &= database.delete(sessionContainer, new Location(0, 0, null));
        deleted &= database.delete(sessionContainer, new WifiMorsel(null, null, 0, 0, null, null));
        if (deleted) {
            database.reinit(sessionContainer);
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("All areas were removed from Database.");
        }
        sessionContainer.rollback();
        sessionContainer.close();
        return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Removal of areas failed.");
    }

    @Override
    public String getTaskName() {
        return "admin_annihilate_area";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
