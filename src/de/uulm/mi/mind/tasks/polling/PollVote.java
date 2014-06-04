package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * @author Tamino Hartmann
 */
public class PollVote extends PollTask<Poll, Information> {
    @Override
    public boolean validateInput(Poll object) {
        // we need the key and any one option to register a vote
        return !(!safeString(object.getKey()) || object.getOptions() == null || object.getOptions().size() == 0);
    }

    @Override
    public Information doWork(Active active, Poll vote, boolean compact) {
        // start by reading the original Poll so that we don't allow poll modifications here except the vote
        Poll filter = new Poll();
        filter.setKey(vote.getKey());
        DataList<Poll> data = database.read(filter);
        if (data == null || data.size() != 1) {
            return new Error(Error.Type.DATABASE, "Poll could not be found!");
        }
        // prepare objects we need
        Poll original = data.get(0);
        User user = ((User) active.getAuthenticated());
        // apply votes to original
        /*
        for (PollOption option : original.getOptions()) {
            for (PollOption)
        }
        */

        return null;
    }

    @Override
    public String getTaskName() {
        return "poll_vote";
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
