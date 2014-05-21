package de.uulm.mi.mind.logic.tasks.user;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class ToggleAdmin extends Task<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        log.error(TAG, "Toggled admin! DANGEROUS OPERATION!");
        User user = ((User) active.getAuthenticated());
        user.setAdmin(!user.isAdmin());

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.update(sessionContainer, user);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("User was updated successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Update of User resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "toggle_admin";
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
