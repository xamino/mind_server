package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann.
 */
public class KillSessions extends Task<None, Information> {
    @Override
    public Information doWork(Active active, None object) {
        Security.clear();
        return new Success("All active sessions have been killed.");
    }

    @Override
    public String getTaskName() {
        return "kill_sessions";
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
    public Class<Information> getOutputType() {
        return Information.class;
    }

    @Override
    public boolean isAdminTask() {
        return true;
    }
}
