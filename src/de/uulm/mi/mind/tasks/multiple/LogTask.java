package de.uulm.mi.mind.tasks.multiple;

import de.uulm.mi.mind.logger.permanent.FileLog;
import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.logger.permanent.LogWorker;
import de.uulm.mi.mind.objects.FileLogObject;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.Task;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 7/15/14.
 * Allows clients to write to the server FileLog.
 */
public class LogTask extends Task<FileLogObject, Sendable> {
    @Override
    public boolean validateInput(FileLogObject object) {
        return safeString(object.getFileName()) && safeString(object.getContent());
    }

    @Override
    public Sendable doWork(Active active, final FileLogObject object, boolean compact) {
        FileLog.getInstance().log(new LogWorker() {
            @Override
            public LogObject logCreate() {
                return object;
            }
        });
        return new Success("Logged.");
    }

    @Override
    public String getTaskName() {
        return "log";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(PublicDisplay.class.getSimpleName());
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<FileLogObject> getInputType() {
        return FileLogObject.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
