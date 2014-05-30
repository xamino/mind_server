package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminUserDelete extends AdminTask<User, Information> {
    @Override
    public Information doWork(Active active, final User user) {
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
                    return new Error(Error.Type.DATABASE, "Deletion of User " + user.getEmail() + " resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "admin_user_delete";
    }

    @Override
    public Class<User> getInputType() {
        return User.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
