package de.uulm.mi.mind.security;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tamino Hartmann
 */
public class Security {

    /**
     * Instance of this class. Get it via getInstance().
     */
    private static Security INSTANCE;
    /**
     * Tag used for all logging that happens within Security.
     */
    private final String TAG = "Security";
    /**
     * Session timeout in milliseconds. Cannot be changed during runtime!
     */
    private final long TIMEOUT = 15 * 60 * 1000;
    /**
     * Instance of log used for the output.
     */
    private Messenger log;
    /**
     * Instance of database.
     */
    private DatabaseController database;
    /**
     * Instance of SecureRandom used for generating hashes.
     */
    private SecureRandom random;
    /**
     * Collection of stored actives.
     */
    private HashMap<String, Active> actives;

    /**
     * Constructor. Security handles its instance by itself, simply use the public methods.
     */
    private Security() {
        this.log = Messenger.getInstance();
        this.database = DatabaseController.getInstance();
        this.random = new SecureRandom();
        this.actives = new HashMap<>();
    }

    /**
     * Method for getting the instance of the class. Security handles its instance by itself, simply use the public methods.
     *
     * @return Instance of this class.
     */
    private static Security getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Security();
        }
        return INSTANCE;
    }

    /**
     * Call this method for securing a session. Returns, if valid, an Active object with which session persistent
     * information can be worked with.
     */
    public static synchronized Active begin(final Authenticated authenticated, final String session) {
        // Switch depending on task we have to do
        if (authenticated == null && session == null) {
            // this means something is wrong, so obviously not secure --> return null
            return null;
        } else if (session != null && authenticated == null) {
            // check a session
            return getInstance().check(session);
        } else {
            // This means we have an authenticated and require a new login (which means we ignore the session)
            return getInstance().login(authenticated);
        }
    }

    /**
     * Call this method when you are done with the session. This will save the Active object so that it will be available
     * for the next call if still secure.
     */
    public static synchronized void finish(final Active active) {
        getInstance().update(active);
    }

    // -------------------------- Actual instanced code here ----------------------------

    /**
     * Returns an Active object if the session is valid, otherwise null.
     *
     * @param session The session to check for.
     * @return The Active object if the session is valid, otherwise null.
     */
    private Active check(final String session) {
        // Make sure all actives are up to date before a check
        enforceTimeout();
        // now check if valid
        Active active = actives.get(session);
        if (active == null) {
            log.log(TAG, "Check failed: no Active instance found.");
            return null;
        }
        // now get database object
        Authenticated databaseSafe = readDB(active.getAuthenticated());
        // check if the user is still legal
        if (databaseSafe == null) {
            log.log(TAG, "Check failed: Authenticated not found.");
            actives.remove(session);
            return null;
        }
        // if valid, update
        active.setAuthenticated(databaseSafe);
        active.setTimestamp(System.currentTimeMillis());
        // set in session list
        actives.put(session, active);
        // and return the correct active
        return active;
    }

    /**
     * Method that tries to login a given Authenticated object. Returns null if it fails, otherwise the Active object
     * is given.
     *
     * @param authenticated The Authenticated that is checked for.
     * @return The Active object if legal, otherwise null.
     */
    private Active login(final Authenticated authenticated) {
        // get safe object
        Authenticated databaseSafe = readDB(authenticated);
        if (databaseSafe == null) {
            log.log(TAG, "Login failed for " + authenticated.readIdentification() + " due to no found legal authenticated!");
            return null;
        }
        // check using BCrypt
        if (!(BCrypt.checkpw(authenticated.readAuthentication(), databaseSafe.readAuthentication()))) {
            log.log(TAG, "Login failed for " + authenticated.readIdentification() + " due to wrong password!");
            return null;
        }
        // check whether first time login
        boolean firstFlag = false;
        if (databaseSafe.getAccessDate() == null) {
            firstFlag = true;
        }
        // try to update last access time
        databaseSafe.setAccessDate(new Date());
        if (!(database.update((Data) databaseSafe))) {
            log.error(TAG, "Login failed for " + authenticated.readIdentification() + " due to error updating access time!");
            return null;
        }
        // generate the session
        String session = new BigInteger(130, random).toString(32);
        // build active
        Active active = new Active(session);
        // set the important values
        active.setAuthenticated(databaseSafe);
        active.setTimestamp(System.currentTimeMillis());
        active.setUnused(firstFlag);
        // add to list
        actives.put(session, active);
        log.log(TAG, "Login of " + authenticated.readIdentification() + ".");
        return active;
    }

    /**
     * Method that updates the Active in the session list, thus ensuring session persistent data.
     *
     * @param active The active object to store.
     */
    private void update(final Active active) {
        if (active == null) {
            log.error(TAG, "Update failed due to missing active object!");
            return;
        }
        // Check for logout
        if (active.isInvalidate()) {
            // must destroy all session of the same user
            ArrayList<Active> remove = new ArrayList<>();
            remove.add(active);
            Active check;
            for (Map.Entry<String, Active> entry : actives.entrySet()) {
                check = entry.getValue();
                if (check.getAuthenticated().readIdentification().equals(active.getAuthenticated().readIdentification())) {
                    remove.add(check);
                }
            }
            // remove all
            log.log(TAG, "Logout of " + active.getAuthenticated().readIdentification() + ". "
                    + remove.size() + " active sessions killed.");
            for (Active toRemove : remove) {
                actives.remove(toRemove.getSESSION());
            }
            return;
        }
        // otherwise update value
        actives.put(active.getSESSION(), active);
    }

    /**
     * Method that checks all the actives' timeout value. If the timeout has been exceeded, the active is removed.
     */
    private void enforceTimeout() {
        ArrayList<Active> remove = new ArrayList<>();
        // Find out which Actives have exceeded the timeout
        Active active;
        for (Map.Entry<String, Active> entry : actives.entrySet()) {
            active = entry.getValue();
            long delta = System.currentTimeMillis() - active.getTimestamp();
            if (delta > TIMEOUT) {
                remove.add(active);
            }
        }
        // remove them
        for (Active toRemove : remove) {
            log.log(TAG, "Session for " + toRemove.getAuthenticated().readIdentification() + " timed out.");
            actives.remove(toRemove.getSESSION());
        }
    }

    /**
     * Helper function for centralized read from DB.
     *
     * @return The Authenticated freshly read if available, else null.
     */
    private Authenticated readDB(Authenticated authenticated) {
        Data data;
        if (authenticated instanceof User) {
            data = database.read(new User(authenticated.readIdentification()));
        } else if (authenticated instanceof PublicDisplay) {
            data = database.read(new PublicDisplay(authenticated.readIdentification(), null, null, 0, 0));
        } else {
            log.error(TAG, "Read from DB failed because of wrong object given!");
            return null;
        }
        if (!(data != null && ((DataList) data).size() == 1)) {
            // log.error(TAG, "Read from DB failed because read object is either missing or ambiguous!");
            return null;
        }
        return (Authenticated) ((DataList) data).get(0);
    }
}
