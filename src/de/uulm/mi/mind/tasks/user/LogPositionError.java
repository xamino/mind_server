package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.permanent.FileLogWrapper;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class LogPositionError extends UserTask<None, Sendable> {

    private final String REAL_POSITION = "realPosition";

    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, None object, boolean compact) {
        // note that we don't check if REAL_POSITION is not null: this is done because no position can be wrong too;
        // we catch that in the FileLogWrapper
        FileLogWrapper.positionError(((User) active.getAuthenticated()), ((Area) active.readData(REAL_POSITION)));
        return new Success("Error has been logged.");
    }

    @Override
    public String getTaskName() {
        return "log_position_error";
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
