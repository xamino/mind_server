package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.PollState;

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
        for (final Poll poll : data) {
            boolean update = false;
            if (new Date().after(new Date(poll.getEnd().getTime() + ENDDELTA)) && poll.getState() != PollState.ENDED) {
                log.log(TAG, "Ending poll " + poll.getQuestion() + "!");
                poll.setState(PollState.ENDED);
                update = true;
            } else if (new Date().after(poll.getEnd()) && poll.getState() != PollState.CLOSED) {
                log.log(TAG, "Closing poll " + poll.getQuestion() + "!");
                poll.setState(PollState.CLOSED);
                update = true;
            }
            if (update) {
                database.open(new Transaction() {
                    @Override
                    public Data doOperations(Session session) {
                        if (!session.update(poll)) {
                            log.error(TAG, "Failed to update poll on read for states!");
                        }
                        return null;
                    }
                });
            }
        }
        // prepare objects we need
        return data;
    }
}
