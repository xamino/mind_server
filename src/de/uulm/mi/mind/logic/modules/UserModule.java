package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.Task;
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
            return new Error(Error.Type.WRONG_OBJECT, "The User Module requires a User Object.");
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

        return new Error(Error.Type.TASK, "The task " + task.toString() + " is not implemented.");
    }

    private Data readUser(User user) {

        // get all Users
        if (user == null) {
            return read(new User(null));
        }
        // get filtered Users
        if (user.getKey() == null) {
            return read(user);
        }
        // from here on only objects with a valid key == single ones are queried
        Data data = read(user);
        if (data instanceof DataList && ((DataList) data).isEmpty()) {
            return new Error(Error.Type.DATABASE, "User could not be found!");
        }
        return data;

    }

    private Data annihilateUsers() {
        Boolean deleted = database.delete(new User(null));
        if (deleted) {
            database.reinit();
            return new Success("All users were removed from Database. Use default admin.");
        }
        return new Error(Error.Type.DATABASE, "Removal of users failed.");
    }
}
