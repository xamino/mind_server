package de.uulm.mi.mind.security;

import java.util.Date;

/**
 * Interface that all objects must implement that can be used to log in to the system.
 * For now, that should only be User and PublicDisplay.
 */
public interface Authenticated {
    /**
     * Reads the ID for the login.
     *
     * @return The ID to check.
     */
    public String readIdentification();

    /**
     * Reads the authentication token, namely the password.
     *
     * @return The hashed password.
     */
    public String readAuthentication();

    /**
     * Method for reading the last time the object was used.
     *
     * @return The date.
     */
    public Date getAccessDate();

    /**
     * Method that sets the last time the object was authenticated.
     *
     * @param accessDate The date it was last used.
     */
    public void setAccessDate(Date accessDate);
}
