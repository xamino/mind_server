package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.PollState;
import de.uulm.mi.mind.objects.messages.Success;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for basic PollTask functionality.
 * Created by Tamino Hartmann on 6/3/14.
 */
abstract public class PollTask<I extends Sendable, O extends Sendable> extends Task<I, O> {

    /**
     * Time from end that poll switches from CLOSED to ENDED.
     */
    private final int ENDDELTA = 30 * 60 * 1000;
    private final String TAG = "PollTask";

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }

    /**
     * Method to read polls from db. Handles state changes and updates because of them.
     *
     * @param filter The read filter.
     * @return The datalist.
     */
    protected DataList<Poll> readValidPoll(Poll filter) {
        DataList<Poll> data = database.read(filter);
        if (data == null) {
            log.error(TAG, "Poll(s) could not be found!");
            return null;
        }
        final DataList<Poll> toUpdate = new DataList<>();
        for (Poll poll : data) {
            // need to catch ended - closed - ended - ... :P
            if (poll.getState() == PollState.ENDED) {
                continue;
            }
            if (new Date().after(new Date(poll.getEnd().getTime() + ENDDELTA)) && poll.getState() != PollState.ENDED) {
                log.log(TAG, "Ending poll " + poll.getQuestion() + "!");
                poll.setState(PollState.ENDED);
                toUpdate.add(poll);
            } else if (new Date().after(poll.getEnd()) && poll.getState() != PollState.CLOSED) {
                log.log(TAG, "Closing poll " + poll.getQuestion() + "!");
                poll.setState(PollState.CLOSED);
                toUpdate.add(poll);
            }
        }
        // update toUpdate list
        if (toUpdate.size() != 0) {
            database.open(new Transaction() {
                @Override
                public Data doOperations(Session session) {
                    for (Poll poll : toUpdate) {
                        if (!session.update(poll)) {
                            log.error(TAG, "Failed to update poll <" + poll.getQuestion() + "> on read for states!");
                        }
                    }
                    return new Success("up up up and away we gooooooOOOOOoooo!");
                }
            });
        }
        // prepare objects we need
        return data;
    }
}
