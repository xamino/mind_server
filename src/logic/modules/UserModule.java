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
    public Data run(String task, Data request) {

        if (!(request instanceof User)) {
            return new Error("WrongDataType", "The User Module requires a User Object.");
        }

        User user = (User) request;

        switch (task) {
            case "createUser":
                return createUser(user);
            case "readUser":
                return readUser(user);
            case "updateUser":
                return updateUser(user);
            case "deleteUser":
                return deleteUser(user);
        }

        return null;
    }

    private Data deleteUser(User user) {
        boolean op = database.delete(user);
        if (op)
            return new Success("UserDeletionSuccess", "The user " + user.getName() + "was deleted successfully.");
        else
            return new Error("UserDeletionFailure", "No idea why");
    }

    private Data updateUser(User user) {
        boolean op = database.update(user);
        if (op)
            return new Success("UserUpdateSuccess", "The user " + user.getName() + "was updated successfully.");
        else
            return new Error("UserUpdateFailure", "No idea why");
    }

    private Data readUser(User user) {
        Data data = database.read(user);
        if (data != null)
            return data;
        else
            return new Error("UserReadFailure", "No idea why");
    }

    private Data createUser(User user) {
        boolean op = database.create(user);
        if (op)
            return new Success("UserCreationSuccess", "The user " + user.getName() + "was deleted successfully.");
        else
            return new Error("UserCreationFailure", "No idea why");
    }
}
