package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class Logout extends Task<Arrival, Information> {
    @Override
    public Information doWork(Active active, Arrival object) {
        Active activeUser = Security.begin(null, object.getSessionHash());
        if (activeUser == null) {
            return new Success(Success.Type.NOTE, "Session is already not valid!");
        }
        activeUser.invalidate();
        Security.finish(activeUser);
        return new Success("Logout successful.");
    }

    @Override
    public String getTaskName() {
        return "logout";
    }

    @Override
    public Set<String> getTaskPermission() {
        return null;
    }

    @Override
    public Class<Arrival> getInputType() {
        return Arrival.class;
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
