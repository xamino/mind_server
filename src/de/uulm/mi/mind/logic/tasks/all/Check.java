package de.uulm.mi.mind.logic.tasks.all;

import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class Check extends Task<Arrival, Information> {

    @Override
    public Information doWork(Active active, Arrival object) {
        Active activeUser = Security.begin(null, object.getSessionHash());
        if (activeUser == null) {
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.SECURITY, "Session invalid!");
        }
        Security.finish(activeUser);
        return new Success("Session is valid.");
    }

    @Override
    public String getTaskName() {
        return "check";
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
