package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.anonymizer.Anonymizer;
import de.uulm.mi.mind.logger.permanent.FileLog;
import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.logger.permanent.LogWorker;
import de.uulm.mi.mind.objects.FileLogObject;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * Created by Tamino Hartmann on 7/17/14.
 * <p/>
 * Logs time that the app screen has remained off. For this task to work, send a FileLogObject where only the content
 * will be read. It should contain the time the smartphone has been inactive.
 */
public class InactiveLog extends UserTask<FileLogObject, Sendable> {
    @Override
    public boolean validateInput(FileLogObject object) {
        return safeString(object.getContent());
    }

    @Override
    public Sendable doWork(final Active active, final FileLogObject object, boolean compact) {
        FileLog.getInstance().log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return "activity";
                    }

                    @Override
                    public String getContent() {
                        String userKey = Anonymizer.getInstance().getKey(active.getAuthenticated());
                        return userKey + " inactive for " + object.getContent();
                    }
                };
            }
        });
        return new Success("Inactivity has been logged.");
    }

    @Override
    public String getTaskName() {
        return "inactive_log";
    }

    @Override
    public Class<FileLogObject> getInputType() {
        return FileLogObject.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
