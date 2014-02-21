package database.objects;

/**
 * @author Tamino Hartmann
 */
public class Arrival {
    private String sessionHash;
    private String task;

    public Arrival(String sessionHash, String task) {
        this.sessionHash = sessionHash;
        this.task = task;
    }

    public String getSessionHash() {
        return sessionHash;
    }

    public void setSessionHash(String sessionHash) {
        this.sessionHash = sessionHash;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}
