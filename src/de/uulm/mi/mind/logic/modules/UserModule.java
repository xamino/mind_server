package de.uulm.mi.mind.logic.modules;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 */
public class UserModule implements Module {

    private final DatabaseController database;

    public UserModule() {
        database = DatabaseController.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {

        if (task == Task.User.ANNIHILATE) {
            return annihilateUsers();
        }

        if (request == null) {
            request = new User(null);
        } else if (!(request instanceof User)) {
            return new Error(Error.Type.WRONG_OBJECT, "User tasks always requires a User Object.");
        }

        User user = (User) request;

        Task.User userTask = (Task.User) task;
        switch (userTask) {
            case READ:
                return readUser(user);
        }

        return new Error(Error.Type.TASK, "The task " + task.toString() + " is not implemented.");
    }

    private Data readUser(User user) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<User> read = database.read(sessionContainer, user);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of User resulted in an error.");
        }

        // get filtered Users
        if (user.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "User could not be found!");
        }
        return read;
    }

    private Data annihilateUsers() {
        ObjectContainer sessionContainer = database.getSessionContainer();
        boolean deleted = database.delete(sessionContainer, new User(null));
        if (deleted) {
            database.reinit(sessionContainer);
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("All users were removed from Database. Use default admin.");
        }
        sessionContainer.rollback();
        sessionContainer.close();
        return new Error(Error.Type.DATABASE, "Removal of users failed.");
    }
}
