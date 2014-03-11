package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Error;
import database.messages.Success;
import database.objects.User;
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
                return create(user);
            case READ:
                return read(user);
            case UPDATE:
                return update(user);
            case DELETE:
                return delete(user);
            case READ_ALL:
                return readAllUsers(new User("", ""));
            case ANNIHILATE:
                return annihilateUsers();
        }

        return null;
    }

    private Data annihilateUsers() {
        Boolean deleted = database.delete(new User("", ""));
        if(deleted){
            database.init();
            return new Success("UserAnnihilationSuccess", "All users were removed from Database. Use default admin.");
        }
        return new Error("UserAnnihilationFailure", "Removal of users failed.");
    }

    private Data readAllUsers(User user) {
        Data data = database.readAll(user);
        if (data != null)
            return data;
        else
            return new Error("UserReadFailure", "Reading of users failed!");
    }
}
