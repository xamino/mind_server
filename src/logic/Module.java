package logic;

import database.Data;
import database.DatabaseController;
import database.messages.Error;
import database.messages.Success;

/**
 * Created by tamino on 2/19/14.
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
            return new Success(filter.getClass().getName() + "CreationSuccess", "The " + filter.toString() + " was created successfully.");
        else
            return new database.messages.Error("UserCreationFailure", "Creation of " + filter.toString() + " failed!");
    }

    protected Data read(Data filter) {
        Data data = DatabaseController.getInstance().read(filter);
        if (data != null)
            return data;
        else
            return new Error(filter.getClass().getName() + "ReadFailure", "Reading of " + filter.toString() + " failed!");
    }

    protected Data update(Data filter) {
        boolean op = DatabaseController.getInstance().update(filter);
        if (op)
            return new Success(filter.getClass().getName() + "UpdateSuccess", "The " + filter.toString() + " was updated successfully.");
        else
            return new Error(filter.getClass().getName() + "UpdateFailure", "Update of " + filter.toString() + " failed!");
    }

    protected Data delete(Data filter) {
        boolean op = DatabaseController.getInstance().delete(filter);
        if (op)
            return new Success(filter.getClass().getName() + "DeletionSuccess", "The " + filter.toString() + " was deleted successfully.");
        else
            return new Error(filter.getClass().getName() + "DeletionFailure", "Deletion of " + filter.toString() + " failed");
    }

}
