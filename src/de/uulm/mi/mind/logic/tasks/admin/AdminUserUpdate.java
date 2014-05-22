package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class AdminUserUpdate extends AdminTask<User, Information> {
    @Override
    public Information doWork(Active active, User tempUser) {
        // todo use only one sessionContainter?
        // check email
        if (!safeString(tempUser.getEmail())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Email must not be empty!");
        }
        // get original
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<User> read = database.read(sessionContainer, new User(tempUser.getEmail()));
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of User resulted in an error.");
        } else if (read.isEmpty() || read.size() != 1) {
            return new Error(Error.Type.DATABASE, "User could not be found!");
        }
        User originalUser = read.get(0);
        // change name
        if (safeString(tempUser.getName())) {
            originalUser.setName(tempUser.getName());
        }
        // change password
        if (safeString(tempUser.getPwdHash())) {
            originalUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
        }
        // change status
        originalUser.setStatus(tempUser.getStatus());
        // change admin status
        originalUser.setAdmin(tempUser.isAdmin());
        // and update
        sessionContainer = database.getSessionContainer();

        boolean success = database.update(sessionContainer, originalUser);

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
        return "admin_user_update";
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
