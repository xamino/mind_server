package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Success;
import database.objects.*;
import database.messages.Error;
import logic.Module;
import logic.Task;

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
                return createUser(user);
            case READ:
                return readUser(user);
            case UPDATE:
                return updateUser(user);
            case DELETE:
                return deleteUser(user);
            case READ_ALL:
                return readAllUsers(new User("", ""));
        }

        return null;
    }


    private Data createUser(User user) {
        boolean op = database.create(user);
        if (op)
            return new Success("UserCreationSuccess", "The " + user.toString() + " was created successfully.");
        else
            return new Error("UserCreationFailure", "Creation of " + user.toString() + " failed!");
    }

    private Data readUser(User user) {
        Data data = database.read(user);
        if (data != null)
            return data;
        else
            return new Error("UserReadFailure", "Reading of " + user.toString() + " failed!");
    }

    private Data readAllUsers(User user) {
        Data data = database.read(user);
        if (data != null)
            return data;
        else
            return new Error("UserReadFailure", "Reading of users failed!");
    }

    private Data updateUser(User user) {
        boolean op = database.update(user);
        if (op)
            return new Success("UserUpdateSuccess", "The " + user.toString() + " was updated successfully.");
        else
            return new Error("UserUpdateFailure", "Update of " + user.toString() + " failed!");
    }

    private Data deleteUser(User user) {
        boolean op = database.delete(user);
        if (op)
            return new Success("UserDeletionSuccess", "The " + user.toString() + " was deleted successfully.");
        else
            return new Error("UserDeletionFailure", "Deletion of " + user.toString() + " failed");
    }
}
