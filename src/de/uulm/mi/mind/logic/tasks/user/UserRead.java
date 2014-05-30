package de.uulm.mi.mind.logic.tasks.user;

import de.uulm.mi.mind.logic.tasks.Task;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Active;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann..
 */
public class UserRead extends Task<None, User> {
    @Override
    public User doWork(Active active, None object) {
        return ((User) active.getAuthenticated()).safeClone();
    }

    @Override
    public String getTaskName() {
        return "user_read";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<User> getOutputType() {
        return User.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
