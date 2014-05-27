package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * @author Tamino Hartmann
 *         Arrival class – structure of incomming requests must conform to this class.
 */
public class Arrival implements Sendable {
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
    private Sendable object;
    /**
     * Stores the IP address of the client for this access.
     */
    private String ipAddress;

    private boolean compact;

    private Arrival() {
    }

    public Arrival(String sessionHash, String task, Sendable object) {
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

    public Sendable getObject() {
        return object;
    }

    public void setObject(Sendable object) {
        this.object = object;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }
}
