package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.PollOption;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.PollState;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * @author Tamino Hartmann
 */
public class PollVote extends PollTask<Poll, Information> {

    private final String TAG = "PollVote";

    @Override
    public boolean validateInput(Poll object) {
        // we need the key and any one option to register a vote
        return !(!safeString(object.getKey()) || object.getOptions() == null);
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
        final Poll original = data.get(0);
        User user = ((User) active.getAuthenticated());
        // check if poll still ongoing
        if (!original.getState().equals(PollState.ONGOING)) {
            return new Error(Error.Type.TASK, "Poll voting is closed!");
        }
        // check options number
        if (vote.getOptions().size() > original.getAllowedOptionSelections()) {
            return new Error(Error.Type.ILLEGAL_VALUE, "You may only vote " + original.getAllowedOptionSelections() + " times!");
        }
        // apply votes to original
        for (PollOption option : original.getOptions()) {
            // find same polloption
            PollOption currentOption = null;
            for (PollOption sentOption : vote.getOptions()) {
                if (option.getKey().equals(sentOption.getKey())) {
                    currentOption = sentOption;
                    break;
                }
            }
            // if still null this option wasn't found, so remove all votes
            if (currentOption == null) {
                while (option.getUsers().contains(user.getEmail())) {
                    log.log(TAG, "Vote for " + option.getKey() + " removed for " + user.getEmail() + ".");
                    option.getUsers().remove(user.getEmail());
                }
                // continue with next option
                continue;
            }
            // if option was found, set vote
            if (!option.getUsers().contains(user.getEmail())) {
                log.log(TAG, "Added new vote to " + original.getKey() + " for " + user.getEmail() + ".");
                option.getUsers().add(user.getEmail());
            } else {
                log.log(TAG, "Vote for " + original.getKey() + " did not change.");
            }
        }
        // done – save poll
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                if (session.update(original)) {
                    return new Success("You have updated your vote on <" + original.getQuestion() + ">.");
                }
                return new Error(Error.Type.DATABASE, "Failed to update Poll in database!");
            }
        });
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
