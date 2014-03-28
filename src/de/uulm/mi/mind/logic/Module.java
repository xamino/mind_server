package de.uulm.mi.mind.logic;

import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
public abstract class Module {

    /**
     * Modules never return null, make sure all methods either return a valid Data object or Success/Error Object.
     *
     * @param task
     * @param request
     * @return The requested Data Object or an Success/Error Message depending on the Task
     */
    public abstract Data run(Task task, Data request);

    protected Data create(Data filter) {
        boolean op = DatabaseController.getInstance().create(filter);
        if (op)
            return new Success("The " + filter.toString() + " was created successfully.");
        else
            return new Error(Error.Type.DATABASE, "Creation of " + filter.toString() + " failed! It may already exist!");
    }

    protected Data read(Data filter) {
        Data data = DatabaseController.getInstance().read(filter);
        if (data != null)
            return data;
        else
            return new Error(Error.Type.DATABASE, "Reading of " + filter.toString() + " failed!");
    }

    protected Data update(Data filter) {
        boolean op = DatabaseController.getInstance().update(filter);
        if (op)
            return new Success("The " + filter.toString() + " was updated successfully.");
        else
            return new Error(Error.Type.DATABASE, "Update of " + filter.toString() + " failed!");
    }

    protected Data delete(Data filter) {
        boolean op = DatabaseController.getInstance().delete(filter);
        if (op)
            return new Success("The " + filter.toString() + " was deleted successfully.");
        else
            return new Error(Error.Type.DATABASE, "Deletion of " + filter.toString() + " failed");
    }

}
