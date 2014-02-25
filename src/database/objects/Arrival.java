package database.objects;

import database.Data;

/**
 * @author Tamino Hartmann
 */
//WARNING: This class is parsed from JSON by hand in Servlet! Remember to keep that up to date when changing methods here!
public class Arrival implements Data {
    private String sessionHash;
    private String task;
    private Data object;

    public Arrival(String sessionHash, String task, Data object) {
        this.sessionHash = sessionHash;
        this.task = task;
        this.object = object;
    }

    public String getSessionHash() {
        return sessionHash;
    }

    public void setSessionHash(String sessionHash) {
        this.sessionHash = sessionHash;
    }

    @Override
    /**
     * Automatically generated toString method.
     */
    public String toString() {
        return "Arrival{" +
                "sessionHash='" + sessionHash + '\'' +
                ", task='" + task + '\'' +
                ", object=" + object +
                '}';
    }

    /**
     * Method that checks if all important values are not null.
     *
     * @return True if all values are set, otherwise false.
     */
    public boolean isValid() {
        return task != null;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Data getObject() {
        return object;
    }

    public void setObject(Data object) {
        this.object = object;
    }
}
