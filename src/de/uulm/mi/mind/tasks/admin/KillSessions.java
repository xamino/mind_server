package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

/**
 * Created by Tamino Hartmann.
 */
public class KillSessions extends AdminTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        Security.clear();
        return new Success("All active sessions have been killed.");
    }

    @Override
    public String getTaskName() {
        return "kill_sessions";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
