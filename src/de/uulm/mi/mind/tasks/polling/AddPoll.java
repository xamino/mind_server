package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

import java.util.Date;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
public class AddPoll extends PollTask<Poll, Information> {
    @Override
    public boolean validateInput(Poll object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, Poll poll, boolean compact) {
        final Poll toSave;
        // if no creation date was sent along we use now
        if (poll.getCreated() == null) {
            toSave = new Poll(poll.getQuestion(), new Date());
        } else {
            // take provided
            toSave = new Poll(poll.getQuestion(), poll.getCreated());
        }
        // Set icon if provided, else default
        if (safeString(poll.getIcon())) {
            toSave.setIcon(poll.getIcon());
        } else {
            // todo default icon?
            toSave.setIcon("default");
        }
        // todo max 4 – why?
        // save occurrences
        toSave.setOccurrences(poll.getOccurrences());
        toSave.setState(poll.getState());

        // save to db
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(toSave);
                if (success) {
                    return new Success("Poll created!");
                } else {
                    return new Error(Error.Type.DATABASE, "Failed to create poll!");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "add_poll";
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
