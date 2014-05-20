package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Authenticated;

import java.util.Collection;

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
    public Collection<Class<? extends Authenticated>> getTaskPermission() {
        return null;
    }

    @Override
    public Class<? extends Sendable> getInputType() {
        return Sendable.class;
    }

    @Override
    public Class<? extends Sendable> getOutputType() {
        return Sendable.class;
    }
}
