package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.UserTask;

import java.io.File;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class IconDelete extends UserTask<None, Information> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Information doWork(Active active, None object, boolean compact) {
        String userID = active.getAuthenticated().readIdentification();
        String icon = "icon_" + userID;
        File file = new File(filePath.iconPath() + icon);
        if (!file.exists()) {
            return new Success(Success.Type.NOTE, "No icon to remove.");
        }
        if (!file.delete()) {
            log.error(TAG, "Failed to remove icon for " + userID + "!");
            return new Error(Error.Type.SERVER, "Failed to delete icon!");
        }
        log.log(TAG, "User " + userID + " deleted icon.");
        return new Success("Icon removed.");
    }

    @Override
    public String getTaskName() {
        return "icon_delete";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
