package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.WifiMorsel;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminAnnihilateArea extends AdminTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        log.log(TAG, "Removing all area, location and morsel objects!");
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                Boolean deleted = session.delete(new Area(null));
                // Delete these to be sure...
                deleted &= session.delete(new Location(0, 0, null));
                deleted &= session.delete(new WifiMorsel(null, null, 0, 0, null, null));
                if (deleted) {
                    session.reinit();
                    return new Success("All areas were removed from Database.");
                }
                return new Error(Error.Type.DATABASE, "Removal of areas, locations or morsels failed. The operation was rolled back.");
            }
        });
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
