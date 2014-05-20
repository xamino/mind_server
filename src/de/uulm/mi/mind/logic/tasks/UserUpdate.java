package de.uulm.mi.mind.logic.tasks;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann.
 */
public class UserUpdate extends Task<User, Information> {
    @Override
    public Information doWork(Active active, User sentUser) {
        // Note that the session user object now needs to be updated. This is done the next time the user
        // sends a request through SecurityModule; it will always get the up to date object from the
        // database.
        User user = ((User) active.getAuthenticated());
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
        user.setStatus(sentUser.getStatus());

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
        return "user_update";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<User> getInputType() {
        return User.class;
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
