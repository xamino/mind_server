package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.AdminTask;

/**
 * @author Tamino Hartmann
 */
public class PollRead extends AdminTask<Poll, Sendable> {
    @Override
    public boolean validateInput(Poll object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Poll object, boolean compact) {
        return database.read(object,3);
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
