package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.logger.permanent.FileLog;
import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.logger.permanent.LogWorker;
import de.uulm.mi.mind.objects.FileLogObject;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

/**
 * @author Tamino Hartmann
 */
public class LogAppVersion extends UserTask<FileLogObject, Information> {
    @Override
    public boolean validateInput(FileLogObject object) {
        return safeString(object.getContent());
    }

    @Override
    public Information doWork(final Active active, final FileLogObject object, boolean compact) {
        FileLog.getInstance().log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return new LogObject() {
                    @Override
                    public String getFileName() {
                        return "version";
                    }

                    @Override
                    public String getContent() {
                        return active.getAuthenticated().readIdentification() + " is at version " + object.getContent();
                    }
                };
            }
        });
        return new Success("Inactivity has been logged.");
    }

    @Override
    public String getTaskName() {
        return "log_app_version";
    }

    @Override
    public Class<FileLogObject> getInputType() {
        return FileLogObject.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
