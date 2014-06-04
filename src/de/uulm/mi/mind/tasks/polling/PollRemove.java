package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * Created by Tamino Hartmann on 6/3/14.
 * Task for removing a poll. Only the admin can delete any polls! Normal users can only delete the ones they are the
 * owner of.
 */
public class PollRemove extends PollTask<Poll, Information> {
    @Override
    public boolean validateInput(Poll object) {
        return true;
    }

    @Override
    public Information doWork(Active active, final Poll object, boolean compact) {
        User user = ((User) active.getAuthenticated());
        // admin requires no further checks, we just hope he knows what he's doing :P
        if (user.isAdmin()) {
            return (Information) database.open(new Transaction() {
                @Override
                public Data doOperations(Session session) {
                    if (session.delete(object)) {
                        return new Success("Poll(s) deleted.");
                    }
                    return new Error(Error.Type.DATABASE, "Failed to delete poll!");
                }
            });
        }
        // otherwise, a user may only delete his own polls
        if (!user.getKey().equals(object.getOwner())) {
            return new Error(Error.Type.SECURITY, "You may only delete polls that you are the owner of!");
        }
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                if (session.delete(object)) {
                    return new Success("Successfully deleted.");
                }
                return new Error(Error.Type.DATABASE, "Failed to delete poll!");
            }
        });
    }

    @Override
    public String getTaskName() {
        return "poll_remove";
    }

    @Override
    public Class<Poll> getInputType() {
        return Poll.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
