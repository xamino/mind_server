package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.security.Authenticated;

import java.util.Collection;

/**
 * @author Tamino Hartmann
 */
public class Echo extends Task<Sendable> {

    @Override
    public Sendable doWork(Sendable object) {
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
}
