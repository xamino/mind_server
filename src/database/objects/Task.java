package database.objects;

/**
 * Created by Cassio on 24.02.14.
 */
public interface Task {

    public enum User implements Task {
        CREATE, READ, UPDATE, DELETE, READ_ALL
    }
}


