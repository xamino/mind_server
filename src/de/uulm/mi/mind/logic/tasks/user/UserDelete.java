package de.uulm.mi.mind.logic.tasks.user;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.Task;
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
 * Created by Tamino Hartmann.
 */
public class UserDelete extends Task<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        active.invalidate();
        final User user = (User) active.getAuthenticated();

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.delete(user);

                if (success) {
                    if (user.getKey() == null) {
                        return new Success("All Users were deleted successfully.");
                    }
                    return new Success("User was deleted successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Deletion of User resulted in an error.");
                }
            }
        });
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
