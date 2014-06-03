package de.uulm.mi.mind.logic.tasks.all;

import de.uulm.mi.mind.logic.tasks.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Authenticated;
import de.uulm.mi.mind.security.Security;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class Login extends Task<Authenticated, Information> {

    @Override
    public boolean validateInput(Authenticated object) {
        return true;
    }

    @Override
    public Information doWork(Active active, Authenticated object, boolean compact) {
        // try login
        Active activeUser = Security.begin(object, null);
        // check if okay
        if (activeUser == null) {
            return new Error(Error.Type.LOGIN, "Login failed. Check identification, authentication, and user type!");
        }
        // otherwise we finish again directly by returning the session
        Security.finish(activeUser);
        // If it was the first login, we send a note instead of just a simple ok so the client can know
        if (activeUser.wasUnused()) {
            return new Success(Success.Type.NOTE, activeUser.getSESSION());
        }
        return new Success(Success.Type.OK, activeUser.getSESSION());
    }

    @Override
    public String getTaskName() {
        return "login";
    }

    @Override
    public Set<String> getTaskPermission() {
        return null;
    }

    @Override
    public Class<Authenticated> getInputType() {
        return Authenticated.class;
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
