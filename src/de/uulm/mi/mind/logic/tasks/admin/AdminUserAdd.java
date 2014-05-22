package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
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
public class AdminUserAdd extends AdminTask<User, Information> {
    @Override
    public Information doWork(Active active, User tempUser) {
        // check email
        if (!safeString(tempUser.getEmail())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Email is primary key! May not be empty.");
        }
        // for security reasons, log this
        if (tempUser.isAdmin()) {
            log.log(TAG, "Adding user " + tempUser.getEmail() + " as admin!");
        }
        // all else we set manually to valid values
        tempUser.setPosition(null);
        tempUser.setAccessDate(null);
        // check & handle password (we do this last because we might need to send back the key)
        if (safeString(tempUser.getPwdHash())) {
            // this means a password was provided
            tempUser.setPwdHash(BCrypt.hashpw(tempUser.getPwdHash(), BCrypt.gensalt(12)));
            // update to DB
            ObjectContainer sessionContainer = database.getSessionContainer();
            boolean success = database.create(sessionContainer, tempUser);
            if (success) {
                sessionContainer.commit();
                sessionContainer.close();
                log.log(TAG, "User " + tempUser.readIdentification() + " has been registered.");
                return new Success("Registered to '" + tempUser.getEmail() + "'.");
            } else {
                // some kind of error occurred
                sessionContainer.rollback();
                sessionContainer.close();
                return new Error(Error.Type.DATABASE, "Creation of User resulted in an error.");
            }
        }
        // this means we generate a password
        String key = generateKey();
        // hash it
        tempUser.setPwdHash(BCrypt.hashpw(key, BCrypt.gensalt(12)));
        // update to DB
        ObjectContainer sessionContainer = database.getSessionContainer();
        boolean success = database.create(sessionContainer, tempUser);
        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            log.log(TAG, "User " + tempUser.readIdentification() + " has been registered.");
            // Send key if successful
            return new Success(Success.Type.NOTE, key);

        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of User resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "admin_user_add";
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
