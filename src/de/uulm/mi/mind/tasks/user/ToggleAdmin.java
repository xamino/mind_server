package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.Task;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class ToggleAdmin extends Task<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        log.error(TAG, "Toggled admin! DANGEROUS OPERATION!");
        final User user = ((User) active.getAuthenticated());
        user.setAdmin(!user.isAdmin());

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.update(user);

                if (success) {
                    return new Success("User was updated successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Update of User resulted in an error.");
                }
            }
        });
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
