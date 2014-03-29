package de.uulm.mi.mind.objects;

/**
 * @author Tamino Hartmann
 *         This class saves all data required for an active user on the server. This includes temporary data or data that is
 *         valid for only this session. We also link the actual persistant data object.
 */
// NOTE not meant to be sent, this is only for the server internally!!!
public class ActiveUser implements Data {
    // todo: add status and settings
    /**
     * Stores the user object (either User or PublicDisplay)
     */
    private Authenticated authenticated;
    /**
     * Stores the session timestamp. This is the time when the user last accessed the system.
     */
    private long sessionTimestamp;
    /**
     * The session hash for validating an active user.
     */
    private String sessionHash;
    /**
     * The last known position of a user, if applicable.
     */
    private Area lastPosition;
    /**
     *
     */
    private Class<? extends Authenticated> authenticatedType;

    public ActiveUser(Authenticated authenticated, long sessionTimestamp, String sessionHash) {
        this.authenticated = authenticated;
        this.sessionTimestamp = sessionTimestamp;
        this.sessionHash = sessionHash;

        setAuthenticatedType();
    }

    private void setAuthenticatedType() {
        if (authenticated instanceof User) {
            this.authenticatedType = User.class;
        } else {
            this.authenticatedType = PublicDisplay.class;
        }
    }

    public Class<? extends Authenticated> getAuthenticatedType() {
        return authenticatedType;
    }

    public Authenticated getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Authenticated authenticated) {
        this.authenticated = authenticated;
        setAuthenticatedType();
    }

    public long getSessionTimestamp() {
        return sessionTimestamp;
    }

    public void setSessionTimestamp(long sessionTimestamp) {
        this.sessionTimestamp = sessionTimestamp;
    }

    public String getSessionHash() {
        return sessionHash;
    }

    public void setSessionHash(String sessionHash) {
        this.sessionHash = sessionHash;
    }

    public Area getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Area lastPosition) {
        this.lastPosition = lastPosition;
    }

    @Override
    public String getKey() {
        return null;
    }
}
