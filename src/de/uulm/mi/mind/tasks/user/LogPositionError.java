package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.logger.permanent.FileLog;
import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.logger.permanent.LogWorker;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
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
    public Sendable doWork(final Active active, final Area object, boolean compact) {
        FileLog.getInstance().log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return "position";
                    }

                    @Override
                    public String getContent() {
                        String userKey = Anonymizer.getInstance().getKey(active.getAuthenticated());
                        String areaKey = Anonymizer.getInstance().getKey(object);
                        return "xxx " + userKey + " @ " + areaKey + " is wrong";
                    }
                };
            }
        });
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
