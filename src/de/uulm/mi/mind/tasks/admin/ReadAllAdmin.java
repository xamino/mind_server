package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

/**
 * @author Tamino Hartmann
 */
public class ReadAllAdmin extends AdminTask<None, Sendable> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, None object, boolean compact) {
        User filter = new User(null);
        filter.setAdmin(true);
        // get
        DataList<User> read = database.read(filter);
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
