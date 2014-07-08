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
public class LogPositionError extends UserTask<Area, Sendable> {

    private final String REAL_POSITION = "realPosition";

    @Override
    public boolean validateInput(Area object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Area object, boolean compact) {
        FileLogWrapper.positionError(((User) active.getAuthenticated()), object);
        return new Success("Error has been logged.");
    }

    @Override
    public String getTaskName() {
        return "log_position_error";
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
