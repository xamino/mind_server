package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * Created by Tamino Hartmann.
 */
public class UserDelete extends UserTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        active.invalidate();
        final User user = (User) active.getAuthenticated();

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.delete(user);

                if (success) {
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
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
