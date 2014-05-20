package de.uulm.mi.mind.logic.tasks;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann.
 */
public class UserDelete extends Task<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        active.invalidate();
        ObjectContainer sessionContainer = database.getSessionContainer();
        boolean success = database.delete(sessionContainer, active.getAuthenticated());

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("User was deleted successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Deletion of User resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "user_delete";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
