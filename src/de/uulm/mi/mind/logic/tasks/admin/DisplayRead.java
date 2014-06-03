package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.logic.tasks.AdminTask;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class DisplayRead extends AdminTask<PublicDisplay, Sendable> {
    @Override
    public boolean validateInput(PublicDisplay object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, PublicDisplay display, boolean compact) {
        DataList<PublicDisplay> read = database.read(display);
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of PublicDisplay resulted in an error.");
        }

        // get filtered locations
        if (display.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "PublicDisplay could not be found!");
        }
        return read;
    }

    @Override
    public String getTaskName() {
        return "display_read";
    }

    @Override
    public Class<PublicDisplay> getInputType() {
        return PublicDisplay.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
