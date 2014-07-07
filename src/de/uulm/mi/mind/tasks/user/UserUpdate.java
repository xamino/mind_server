package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logger.permanent.FileLogWrapper;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * Created by Tamino Hartmann.
 */
public class UserUpdate extends UserTask<User, Information> {
    @Override
    public boolean validateInput(User object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, User sentUser, boolean compact) {
        // Note that the session user object now needs to be updated. This is done the next time the user
        // sends a request through SecurityModule; it will always get the up to date object from the
        // database.
        final User user = ((User) active.getAuthenticated());
        // Email is primary key, thus can not be changed!
        if (!sentUser.getEmail().equals(user.getEmail())) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.ILLEGAL_VALUE, "Email can not be changed in an existing user");
        }
        // Make sure that you can't set or unset yourself to admin
        if (sentUser.isAdmin() && !user.isAdmin()) {
            return new Error(Error.Type.ILLEGAL_VALUE, "You do not have the rights to modify your permissions!");
        }
        // password
        if (this.safeString(sentUser.getPwdHash())) {
            // hash:
            user.setPwdHash(BCrypt.hashpw(sentUser.getPwdHash(), BCrypt.gensalt(12)));
        }
        // name
        if (safeString(sentUser.getName())) {
            user.setName(sentUser.getName());
        }
        if (sentUser.getStatus() != null && !sentUser.getStatus().equals(user.getStatus())) {
            user.setStatus(sentUser.getStatus());
            FileLogWrapper.statusUpdate(user);
        }


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
        return "user_update";
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
