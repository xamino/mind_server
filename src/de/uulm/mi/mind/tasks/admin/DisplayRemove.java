package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class DisplayRemove extends AdminTask<PublicDisplay, Information> {
    @Override
    public boolean validateInput(PublicDisplay object) {
        return true;
    }

    @Override
    public Information doWork(Active active, final PublicDisplay display, boolean compact) {
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session dba) {
                boolean success = dba.delete(display);
                if (success) {
                    if (display.getKey() == null) {
                        return new Success("All PublicDisplay were deleted successfully.");
                    }
                    return new Success("PublicDisplay was deleted successfully.");
                } else {
                    return new Error(Error.Type.DATABASE, "Deletion of PublicDisplay resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "display_remove";
    }

    @Override
    public Class<PublicDisplay> getInputType() {
        return PublicDisplay.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
