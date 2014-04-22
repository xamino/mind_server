package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         Arrival class – structure of incomming requests must conform to this class.
 */
public class Arrival implements Data {
    /**
     * The session hash of the client.
     */
    private String sessionHash;
    /**
     * The API task to do.
     */
    private String task;
    /**
     * Voluntary object with which to work with during a task.
     */
    private Data object;
    /**
     * Stores the IP address of the client for this access.
     */
    private String ipAddress;

    private Arrival() {
    }

    public Arrival(String sessionHash, String task, Data object) {
        this.sessionHash = sessionHash;
        this.task = task;
        this.object = object;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSessionHash() {
        return sessionHash;
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

    @Override
    public String getKey() {
        return null;
    }
}
