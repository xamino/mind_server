package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
public class AddPoll extends PollTask<Poll, Information> {
    @Override
    public boolean validateInput(Poll object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, Poll object, boolean compact) {
        return new Success(Success.Type.NOTE, "Not yet implemented...");
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
