package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseManager;
import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 */
public class UserModule implements Module {

    private final DatabaseManager database;

    public UserModule() {
        database = DatabaseManager.getInstance();
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
            case CREATE:
                return createUser(user);
            case READ:
                return readUser(user);
            case UPDATE:
                return updateUser(user);
            case DELETE:
                return deleteUser(user);
        }

        return new Error(Error.Type.TASK, "The task " + task.toString() + " is not implemented.");
    }

    private Data createUser(final User user) {
        if (user.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "User to be created was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(user);

                if (success) {
                    return new Success("User was created successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Creation of User resulted in an error.");
                }
            }
        });

    }

    private Data readUser(final User user) {
        DataList<User> read = (DataList<User>) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                return session.read(user);
            }
        });

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

    private Data updateUser(final User user) {
        if (user.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "User to be updated was null!");
        }

        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.update(user);

                if (success) {
                    return new Success("User was updated successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Update of User resulted in an error.");
                }
            }
        });
    }

    private Data deleteUser(final User user) {
        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.delete(user);

                if (success) {
                    if (user.getKey() == null) {
                        return new Success("All Users were deleted successfully.");
                    }
                    return new Success("User was deleted successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Deletion of User resulted in an error.");
                }
            }
        });
    }

    private Data annihilateUsers() {
        return database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean deleted = session.delete(new User(null));
                if (deleted) {
                    session.reinit();
                    return new Success("All users were removed from Database. Use default admin.");
                }
                return new Error(Error.Type.DATABASE, "Removal of users failed.");
            }
        });
    }
}
