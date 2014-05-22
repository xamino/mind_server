package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class ReadAllAdmin extends AdminTask<None, Sendable> {
    @Override
    public Sendable doWork(Active active, None object) {
        User filter = new User(null);
        filter.setAdmin(true);
        // get
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<User> read = database.read(sessionContainer, filter);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of User resulted in an error.");
        } else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "User could not be found!");
        }
        return read;
    }

    @Override
    public String getTaskName() {
        return "read_all_admin";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
