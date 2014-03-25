package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.logic.Task;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 */
public class UserModule extends Module {

    private final DatabaseController database;

    public UserModule() {
        database = DatabaseController.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (!(request instanceof User) && request != null) {
            return new Error("WrongDataType", "The User Module requires a User Object.");
        }

        User user = (User) request;

        Task.User userTask = (Task.User) task;
        switch (userTask) {
            case CREATE:
                return create(user);
            case READ:
                return readUser(user);
            case UPDATE:
                return update(user);
            case DELETE:
                return delete(user);
            case ANNIHILATE:
                return annihilateUsers();
        }

        return new Error("UserTaskNotImplemented", "The task " + task.toString() + " is not implemented.");
    }

    private Data readUser(User user) {

        // get all Users
        if (user == null) {
            return read(new User(null, null));
        }
        // get filtered Users
        if (user.getEmail() == null) {
            return read(user);
        }

        // from here on single users were requested
        Data data = read(user);
        if (data instanceof DataList) {
            if (((DataList) data).isEmpty()) {
                return new Error("UserMissing", "User could not be found!");
            }
            return data;
        } else {
            return data; // Error
        }
    }

    private Data annihilateUsers() {
        Boolean deleted = database.deleteAll(new User(null, null));
        if (deleted) {
            database.reinit();
            return new Success("UserAnnihilationSuccess", "All users were removed from Database. Use default admin.");
        }
        return new Error("UserAnnihilationFailure", "Removal of users failed.");
    }
}
