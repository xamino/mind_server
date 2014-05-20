package de.uulm.mi.mind.logic.tasks;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
// todo how to check for admin?
public class AdminReadSessions extends Task<None, DataList> {
    @Override
    public DataList doWork(Active active, None object) {
        return Security.readActiveUsers();
    }

    @Override
    public String getTaskName() {
        return "admin_read_sessions";
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
    public Class<DataList> getOutputType() {
        return DataList.class;
    }

    @Override
    public boolean isAdminTask() {
        return true;
    }
}
