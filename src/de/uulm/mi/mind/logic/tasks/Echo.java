package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.security.Active;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class Echo extends Task<Sendable, Sendable> {

    @Override
    public Sendable doWork(Active active, Sendable object) {
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
