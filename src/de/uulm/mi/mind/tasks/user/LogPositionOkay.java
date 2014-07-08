package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.permanent.FileLogWrapper;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class LogPositionOkay extends UserTask<Area, Sendable> {

    @Override
    public boolean validateInput(Area object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Area object, boolean compact) {
        // note that we don't check if REAL_POSITION is not null: this is done because no position can be wrong too;
        // we catch that in the FileLogWrapper
        FileLogWrapper.positionOkay(((User) active.getAuthenticated()), object);
        return new Success("Okay has been logged.");
    }

    @Override
    public String getTaskName() {
        return "log_position_okay";
    }

    @Override
    public Class<Area> getInputType() {
        return Area.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
