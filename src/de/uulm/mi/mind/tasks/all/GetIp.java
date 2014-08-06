package de.uulm.mi.mind.tasks.all;

import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.Task;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class GetIp extends Task<Arrival, Sendable> {

    @Override
    public boolean validateInput(Arrival object) {
        return safeString(object.getSessionHash());
    }

    @Override
    public Sendable doWork(Active active, Arrival object, boolean compact) {
        return object;
    }

    @Override
    public String getTaskName() {
        return "get_ip";
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
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
