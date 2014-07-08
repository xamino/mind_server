package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public abstract class UserTask<I extends Sendable, O extends Sendable> extends Task<I, O> {
    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
