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
                return user == null ? read(new User(null, null)) : read(user);
            case UPDATE:
                return update(user);
            case DELETE:
                return delete(user);
            case ANNIHILATE:
                return annihilateUsers();
        }

        return new Error("UserTaskNotImplemented","The task " + task.toString() + " is not implemented.");
    }

    private Data annihilateUsers() {
        Boolean deleted = database.deleteAll(new User(null, null));
        if (deleted) {
            database.init();
            return new Success("UserAnnihilationSuccess", "All users were removed from Database. Use default admin.");
        }
        return new Error("UserAnnihilationFailure", "Removal of users failed.");
    }
}
