package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class ToggleAdmin extends UserTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        return new Error(Error.Type.SECURITY, "Task has been deactivated for deployment!");
        /*
        log.error(TAG, "Toggled admin! DANGEROUS OPERATION!");
        final User user = ((User) active.getAuthenticated());
        user.setAdmin(!user.isAdmin());

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.update(user);

                if (success) {
                    return new Success("User was updated successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Update of User resulted in an error.");
                }
            }
        });
        */
    }

    @Override
    public String getTaskName() {
        return "toggle_admin";
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
