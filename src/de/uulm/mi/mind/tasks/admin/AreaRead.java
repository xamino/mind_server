package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.WifiMorsel;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AreaRead extends AdminTask<Area, Sendable> {
    @Override
    public boolean validateInput(Area object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Area area, boolean compact) {
        DataList<Area> read = database.read(area);
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of area resulted in an error.");
        }

        // If compact is set, purge all wifimorsels
        if (compact) {
            for (Area area1 : read) {
                for (Location location : area1.getLocations()) {
                    location.setWifiMorsels(new DataList<WifiMorsel>());
                }
            }
        }

        // get filtered Areas
        if (area.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "Area could not be found!");
        }
        return read;
    }

    @Override
    public String getTaskName() {
        return "area_read";
    }

    @Override
    public Class<Area> getInputType() {
        return Area.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}