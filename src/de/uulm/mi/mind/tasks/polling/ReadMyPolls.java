package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * Created by Tamino Hartmann on 6/3/14.
 * Reads a users own polls.
 */
public class ReadMyPolls extends PollTask<None, Sendable> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, None object, boolean compact) {
        Poll filter = new Poll(null);
        filter.setOwner(((User) active.getAuthenticated()));
        return database.read(filter);
    }

    @Override
    public String getTaskName() {
        return "read_my_polls";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
