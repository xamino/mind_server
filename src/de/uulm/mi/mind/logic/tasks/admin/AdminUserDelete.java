package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminUserDelete extends AdminTask<User, Information> {
    @Override
    public Information doWork(Active active, User user) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        boolean success = database.delete(sessionContainer, user);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("User was deleted successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Deletion of User " + user.getEmail() + " resulted in an error.");
        }
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
