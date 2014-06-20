package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * @author Tamino Hartmann
 */
public class PollRead extends PollTask<Poll, Sendable> {
    @Override
    public boolean validateInput(Poll object) {
        // Since this method is meant to filter, we want an object
        return object != null;
    }

    @Override
    public Sendable doWork(Active active, Poll object, boolean compact) {
        return database.read(object, 5);
    }

    @Override
    public String getTaskName() {
        return "poll_read";
    }

    @Override
    public Class<Poll> getInputType() {
        return Poll.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
