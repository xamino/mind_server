package de.uulm.mi.mind.objects.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 * <p/>
 * Abstract class for Admin tasks. Implements the User.class with admin as true for the Task class.
 */
public abstract class AdminTask<I extends Sendable, O extends Sendable> extends Task<I, O> {
    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public boolean isAdminTask() {
        return true;
    }
}
