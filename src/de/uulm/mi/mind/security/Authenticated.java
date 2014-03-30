package de.uulm.mi.mind.security;

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
}
