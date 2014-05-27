package de.uulm.mi.mind.logic.tasks.multiple;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 * <p/>
 * Task for reading all areas in the system. Available for all users and public displays.
 */
public class ReadAllAreas extends Task<None, Sendable> {
    @Override
    public Sendable doWork(Active active, None object) {
        Area filter = new Area(null);
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<Area> read = database.read(sessionContainer, filter);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of area resulted in an error.");
        }

        if (compact) {
            for (Area area : read) {
                for (Location location : area.getLocations()) {
                    location.setWifiMorsels(null);
                }
            }
        }

        return read;
    }

    @Override
    public String getTaskName() {
        return "read_all_areas";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(PublicDisplay.class.getSimpleName());
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
