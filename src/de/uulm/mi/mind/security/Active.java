package de.uulm.mi.mind.security;

import java.util.HashMap;

/**
 * @author Tamino Hartmann
 */
public class Active {
    /**
     * Session of this active object. Can not be changed by the user; the value is set by Security.
     */
    private final String SESSION;
    /**
     * The freshly read Authenticated object of the associated secured object.
     */
    private Authenticated authenticated;
    /**
     * The timestamp of the last legal access of this Active. Can not be manually set.
     */
    private long timestamp;
    /**
     * If set this active will be logged out once done with it.
     */
    private boolean invalidated = false;
    /**
     * This flag is true if the user object has never been accessed before upon begin (for example upon first login).
     */
    private boolean unused = false;
    /**
     * HashMap for session persistant data attached to the Active object.
     */
    private HashMap<String, Object> sessionData;

    protected Active(String session) {
        this.SESSION = session;
        this.sessionData = new HashMap<>();
    }

    public boolean wasUnused() {
        return unused;
    }

    protected void setUnused(boolean unused) {
        this.unused = unused;
    }

    public boolean isInvalidate() {
        return invalidated;
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public long getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Authenticated getAuthenticated() {
        return authenticated;
    }

    protected void setAuthenticated(Authenticated authenticated) {
        this.authenticated = authenticated;
    }

    public String getSESSION() {

        return SESSION;
    }

    /**
     * Method for writing session persistant data to the Active object.
     *
     * @param key   The key for the data.
     * @param value The data.
     */
    public void writeData(final String key, final Object value) {
        sessionData.put(key, value);
    }

    /**
     * Method for reading session persistant data from the Active object.
     *
     * @param key The key for the data.
     * @return The data, if found.
     */
    public Object readData(final String key) {
        return sessionData.get(key);
    }
}
