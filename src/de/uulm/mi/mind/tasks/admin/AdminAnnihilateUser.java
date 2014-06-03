package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class AdminAnnihilateUser extends AdminTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        log.log(TAG, "Removing all users!");
        Security.clear();
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean deleted = session.delete(new User(null));
                if (deleted) {
                    session.reinit();
                    return new Success("All users were removed from Database. Use default admin.");
                }
                return new Error(Error.Type.DATABASE, "Removal of users failed.");
            }
        });
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
