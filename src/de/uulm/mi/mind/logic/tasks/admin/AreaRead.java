package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AreaRead extends AdminTask<Area, Sendable> {
    @Override
    public Sendable doWork(Active active, Area area) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<Area> read = database.read(sessionContainer, area);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of area resulted in an error.");
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
