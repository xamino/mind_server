package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.PollState;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 6/3/14.
 * Task for reading all available polls. Useable by User and PublicDisplay.
 */
public class ReadAllPolls extends PollTask<None, Sendable> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    /**
     * Reads all polls that are ongoing and closed – meaning all polls except ended ones.
     *
     * @param active  The active user object to work with.
     * @param object  The object requested.
     * @param compact Whether a compact answer is wished for or not.
     * @return The polls.
     */
    // todo ended polls can't be seen – when do we delete them?
    @Override
    public Sendable doWork(Active active, None object, boolean compact) {
        DataList<Poll> polls = readValidPoll(new Poll());
        if (polls == null) {
            return new Error(Error.Type.DATABASE, "Database read all polls failed!");
        }
        DataList<Poll> filteredPolls = new DataList<>();
        for (Poll poll : polls) {
            if (poll.getState() != PollState.ENDED) {
                filteredPolls.add(poll);
            }
        }
        return filteredPolls;
    }

    @Override
    public String getTaskName() {
        return "read_all_polls";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    /**
     * We override this method because PublicDisplays can also request polls.
     *
     * @return
     */
    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        permissible.add(PublicDisplay.class.getSimpleName());
        return permissible;
    }
}
