package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.managers.TaskManager;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.AdminTask;

import java.util.Map;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class ReadTasks extends AdminTask<None, Success> {
    @Override
    public boolean validateInput(None object) {
        return true;
    }

    @Override
    public Success doWork(Active active, None object, boolean compact) {
        String answer = "";
        for (Map.Entry<String, Set<String>> entry : TaskManager.readRights().entrySet()) {
            String temp = "public";
            if (entry.getValue() != null && entry.getValue().size() != 0) {
                temp = "";
                boolean first = true;
                for (String option : entry.getValue()) {
                    if (first) {
                        temp += ""+ option;
                        first = false;
                    } else {
                        temp += ", " + option;
                    }
                }
            }
            answer += "Task <" + entry.getKey() + "> is runnable for " + temp + ".\n";
        }
        return new Success(Success.Type.NOTE, answer);
    }

    @Override
    public String getTaskName() {
        return "read_tasks";
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Success> getOutputType() {
        return Success.class;
    }
}
