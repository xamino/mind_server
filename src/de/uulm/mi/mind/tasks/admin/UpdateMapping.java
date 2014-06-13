package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.AdminTask;

/**
 * Created by Cassio on 12.06.2014.
 */
public class UpdateMapping extends AdminTask<None,Information> {

    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                DataList<Location> locations = session.read(new Location(0, 0, null));
                DataList<Area> areas = session.read(new Area(null));

                long time = System.currentTimeMillis();
                for (Area area : areas) {
                    area.setLocations(new DataList<Location>());
                    for (Location location : locations) {
                        if (area.contains(location.getCoordinateX(), location.getCoordinateY())) {
                            area.addLocation(location);
                        }
                    }
                    // must write data back to DB
                    if (!session.update(area)) {
                        log.error(TAG, "Failed to update mapping in DB for " + area.getID() + "!");
                        return new Error(Error.Type.DATABASE,"Failed to update mapping in area: " + area.getID());
                    }
                }

                log.log(TAG, "Updated Location <--> Area mapping. Took " + (System.currentTimeMillis() - time) + "ms.");
                return new Success("Areas updated");
            }
        });
    }

    @Override
    public String getTaskName() {
        return "update_mapping";
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
