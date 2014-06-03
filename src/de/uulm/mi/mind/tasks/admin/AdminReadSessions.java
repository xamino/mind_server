package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;
import de.uulm.mi.mind.tasks.AdminTask;

/**
 * @author Tamino Hartmann
 */
// todo how to check for admin?
public class AdminReadSessions extends AdminTask<None, DataList> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public DataList doWork(Active active, None object, boolean compact) {
        return Security.readActiveUsers();
    }

    @Override
    public String getTaskName() {
        return "admin_read_sessions";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<DataList> getOutputType() {
        return DataList.class;
    }
}
