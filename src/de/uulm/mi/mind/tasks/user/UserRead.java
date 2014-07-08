package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * Created by Tamino Hartmann..
 */
public class UserRead extends UserTask<None, User> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public User doWork(Active active, None object, boolean compact) {
        return ((User) active.getAuthenticated()).safeClone();
    }

    @Override
    public String getTaskName() {
        return "user_read";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<User> getOutputType() {
        return User.class;
    }
}
