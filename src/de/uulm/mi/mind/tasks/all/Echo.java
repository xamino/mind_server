package de.uulm.mi.mind.tasks.all;

import de.uulm.mi.mind.tasks.Task;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.security.Active;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class Echo extends Task<Sendable, Sendable> {

    @Override
    public boolean validateInput(Sendable object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Sendable object, boolean compact) {
        return object;
    }

    @Override
    public String getTaskName() {
        return "echo";
    }

    @Override
    public Set<String> getTaskPermission() {
        return null;
    }

    @Override
    public Class<Sendable> getInputType() {
        return Sendable.class;
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
