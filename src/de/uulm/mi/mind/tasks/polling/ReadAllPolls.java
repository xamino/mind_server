package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
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

    @Override
    public Sendable doWork(Active active, None object, boolean compact) {
        return (Sendable) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                DataList<Poll> list = session.read(new Poll(null));
                if (list == null) {
                    return new Error(Error.Type.DATABASE, "Reading of polls failed!");
                }
                return list;
            }
        });
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
