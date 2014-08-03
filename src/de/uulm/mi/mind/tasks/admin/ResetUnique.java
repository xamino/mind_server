package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.AdminTask;

/**
 * Created by Tamino Hartmann on 8/3/14.
 * <p/>
 * This task resets all unique IDs in the database for User objects. This is mainly useful for restarting a new logging
 * round with new unique anonymous IDs.
 */
public class ResetUnique extends AdminTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        return ((Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // read all users, max depth because otherwise we loose data!
                DataList<User> users = database.read(new User(null), 11);
                for (final User user : users) {
                    // reset unique
                    user.setUnique(null);
                    if (!session.update(user)) {
                        log.error(TAG, "Failed to reset unique for user " + user.getKey() + "!");
                    }
                }
                // finally we MUST reset Anonymizer!
                Anonymizer.getInstance().resetCache();
                return new Success("Successfully reset all User's <unique> field.");
            }
        }));
    }

    @Override
    public String getTaskName() {
        return "reset_unique";
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
