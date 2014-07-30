package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.logger.permanent.FileLog;
import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.logger.permanent.LogWorker;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class LogPositionOkay extends UserTask<Arrival, Sendable> {

    @Override
    public boolean validateInput(Arrival object) {
        return object.getObject() instanceof Area;
    }

    @Override
    public Sendable doWork(final Active active, final Arrival object, boolean compact) {
        // note that we don't check if REAL_POSITION is not null: this is done because no position can be wrong too;
        // we catch that in the FileLogWrapper
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
                        String areaKey = Anonymizer.getInstance().getKey(((Area) object.getObject()));
                        return "ooo " + userKey + " @ " + areaKey + " is okay >> " + object.getDeviceType();
                    }
                };
            }
        });
        return new Success("Okay has been logged.");
    }

    @Override
    public String getTaskName() {
        return "log_position_okay";
    }

    @Override
    public Class<Arrival> getInputType() {
        return Arrival.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
