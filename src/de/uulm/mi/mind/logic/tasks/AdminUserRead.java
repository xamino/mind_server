package de.uulm.mi.mind.logic.tasks;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminUserRead  extends Task<User, Sendable> {
    @Override
    public Sendable doWork(Active active, User object) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<User> read = database.read(sessionContainer, object);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of User resulted in an error.");
        } else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "User could not be found!");
        }
        Data message = checkDataMessage(read, DataList.class);
        if (message == null) {
            DataList<User> users = new DataList<>();
            for (User bum : read) {
                users.add(bum.safeClone());
            }
            return users;
        }
        return nullMessageCatch(message);
    }

    /**
     * Method that checks data returned from a module. The method returns null except when: data is an Information
     * interface type class, data is null, or data is not of type class.
     *
     * @param data  The data to check.
     * @param clazz The class type against which to check.
     * @return An Information object if data is such, else null.
     */
    private Information checkDataMessage(Data data, Class clazz) {
        if (data == null) {
            return new Error(Error.Type.NULL, "Data requested returned NULL, should NOT HAPPEN!");
        } else if (data instanceof Information) {
            return (Information) data;
        } else if (data.getClass() != clazz && !clazz.isAssignableFrom(data.getClass())) {
            return new Error(Error.Type.CAST, "Returned data failed class test!");
        } else {
            // This means everything was okay
            return null;
        }
    }

    @Override
    public String getTaskName() {
        return "admin_user_read";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<User> getInputType() {
        return User.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return true;
    }
}
