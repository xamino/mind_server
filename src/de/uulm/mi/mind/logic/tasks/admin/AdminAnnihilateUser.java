package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminAnnihilateUser extends AdminTask<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        log.log(TAG, "Removing all users!");
        Security.clear();
        ObjectContainer sessionContainer = database.getSessionContainer();
        boolean deleted = database.delete(sessionContainer, new User(null));
        if (deleted) {
            database.reinit(sessionContainer);
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("All users were removed from Database. Use default admin.");
        }
        sessionContainer.rollback();
        sessionContainer.close();
        return new Error(Error.Type.DATABASE, "Removal of users failed.");
    }

    @Override
    public String getTaskName() {
        return "admin_annihilate_user";
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
