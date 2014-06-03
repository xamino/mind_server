package de.uulm.mi.mind.tasks.all;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.Task;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

import java.util.Date;
import java.util.Set;

/**
 * Created by Tamino Hartmann.
 */
public class Registration extends Task<User, Information> {
    @Override
    public boolean validateInput(User object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, final User user, boolean compact) {
        // if anything other than open is stated here, registration is considered closed
        if (!"open".equals(configuration.getRegistration())) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.SECURITY, "Registration is not open! Please contact the admin.");
        }
        // password should be okay
        // note that we do not allow key generation for registration
        if (!safeString(user.readAuthentication())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Password may not be empty!");
        }
        // hash pwd
        user.setPwdHash(BCrypt.hashpw(user.getPwdHash(), BCrypt.gensalt(12)));
        // set access time
        user.setAccessDate(new Date());
        // create user

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(user);

                if (success) {
                    log.log(TAG, "User " + user.readIdentification() + " has been registered.");
                    return new Success("Registered to '" + user.getEmail() + "'.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Creation of User resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "registration";
    }

    @Override
    public Set<String> getTaskPermission() {
        return null;
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
