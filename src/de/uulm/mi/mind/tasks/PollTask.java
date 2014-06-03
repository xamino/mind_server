package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
abstract public class PollTask<I extends Sendable, O extends Sendable> extends Task<I, O> {
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
