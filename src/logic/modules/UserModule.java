package logic.modules;

import database.Data;
import database.DatabaseController;
import database.objects.*;
import database.objects.Error;
import logic.Module;

/**
 * Created by Cassio on 21.02.14.
 */
public class UserModule implements Module {

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
                return readUser(new User("",""));
        }

        return null;
    }

    private Data deleteUser(User user) {
        boolean op = database.delete(user);
        if (op)
            return new Success("UserDeletionSuccess", "The user " + user.getName() + " was deleted successfully.");
        else
            return new Error("UserDeletionFailure", "Deletion of user " + user.getName() + " failed");
    }

    private Data updateUser(User user) {
        boolean op = database.update(user);
        if (op)
            return new Success("UserUpdateSuccess", "The user " + user.getName() + " was updated successfully.");
        else
            return new Error("UserUpdateFailure", "Update of user " + user.getName() + " failed!");
    }

    private Data readUser(User user) {
        Data data = database.read(user);
        if (data != null)
            return data;
        else
            return new Error("UserReadFailure", "Reading of user " + user.getName() + " failed!");
    }

    private Data createUser(User user) {
        boolean op = database.create(user);
        if (op)
            return new Success("UserCreationSuccess", "The user " + user.getName() + " was created successfully.");
        else
            return new Error("UserCreationFailure", "Creation of user " + user.getName() + " failed!");
    }
}
