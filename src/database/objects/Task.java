package database.objects;

/**
 * Created by Cassio on 24.02.14.
 */
public interface Task {

    public enum UserTask implements Task {
        CREATE_USER, READ_USER, UPDATE_USER, DELETE_USER
    }
}


