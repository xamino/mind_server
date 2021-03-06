package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class ForbidLogging extends UserTask<None, Information> {

    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(final Active active, None object, boolean compact) {
        final User user = ((User) active.getAuthenticated());
        if (!user.isLog()) {
            // no need for db if already set
            return new Success(Success.Type.NOTE, "Logging was already forbidden.");
        }
        user.setLog(false);
        return ((Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                if (session.update(user)) {
                    return new Success("Logging has been forbidden.");
                }
                return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Failed to update logging status in database.");
            }
        }));
    }

    @Override
    public String getTaskName() {
        return "forbid_logging";
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
