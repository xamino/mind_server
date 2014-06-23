package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.enums.PollState;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.AdminTask;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Task removes all ended polls from server.
 */
public class PollClean extends AdminTask<None, Information> {

    private String TAG = "PollCleaning";

    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        final Poll filter = new Poll();
        filter.setState(PollState.ENDED);
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                if (session.delete(filter)) {
                    log.log(TAG, "All ended polls have been removed.");
                    return new Success("Polls have undergone spring cleaning!");
                }
                return new Error(Error.Type.DATABASE, "Failed to remove all ended polls.");
            }
        });
    }

    @Override
    public String getTaskName() {
        return "poll_clean";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
