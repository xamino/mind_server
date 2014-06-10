package de.uulm.mi.mind.security;

import de.uulm.mi.mind.io.DatabaseManager;
import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.unsendable.TimedQueue;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Class provides secure session based on hash or login information.
 */
// todo Limit max number of sessions per user!
public class Security {

    /**
     * Instance of this class. Get it via getInstance().
     */
    private static Security INSTANCE;
    /**
     * Tag used for all logging that happens within Security.
     */
    private static final String TAG = "Security";
    /**
     * Session timeout in milliseconds. Cannot be changed during runtime!
     */
    private final static long TIMEOUT = 15 * 60 * 1000;
    /**
     * Instance of log used for the output.
     */
    private static Messenger log;
    /**
     * Instance of database.
     */
    private DatabaseManager database;
    /**
     * Instance of SecureRandom used for generating hashes.
     */
    private SecureRandom random;
    /**
     * Collection of stored actives.
     */
    private TimedQueue<String, Active> actives;

    /**
     * Constructor. Security handles its instance by itself, simply use the public methods.
     */
    private Security() {
        this.log = Messenger.getInstance();
        this.database = DatabaseManager.getInstance();
        this.random = new SecureRandom();
        this.actives = new TimedQueue<>(TIMEOUT);
        log.log(TAG, "Created new instance.");
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
        if (active == null) {
            return;
        }
        getInstance().update(active);
    }

    /**
     * Method that returns all currently logged in users.
     *
     * @return ArrayList containing all authenticated user types.
     */
    public static synchronized DataList<Authenticated> readActiveUsers() {
        DataList<Authenticated> list = new DataList<>();
        for (Active active : getInstance().actives.getValues()) {
            list.add(active.getAuthenticated());
        }
        return list;
    }

    /**
     * Method that returns an ArrayList of all current Active objects. NOTE that this is NOT meant to be used outside
     * of the server, so don't send it to the client!
     *
     * @return The ArrayList with all useable Active objects.
     */
    public static synchronized ArrayList<Active> readActives() {
        return new ArrayList<>(getInstance().actives.getValues());
    }

    /**
     * Method that destroys all sessions.
     */
    public static void clear() {
        INSTANCE = new Security();
    }

    //
    // -------------------------- Actual instanced code here ----------------------------
    //

    /**
     * Returns an Active object if the session is valid, otherwise null.
     *
     * @param session The session to check for.
     * @return The Active object if the session is valid, otherwise null.
     */
    private Active check(final String session) {
        // now check if valid
        final Active active = actives.get(session);
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
        actives.add(session, active);
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
        if (authenticated == null || authenticated.readIdentification() == null || authenticated.readAuthentication() == null) {
            log.log(TAG, "Unable to log in a NULL object or an object with NULL fields!");
            return null;
        }
        // get safe object
        final Authenticated databaseSafe = readDB(authenticated);
        if (databaseSafe == null) {
            log.log(TAG, "Login failed for " + authenticated.readIdentification() + " due to no found legal authenticated!");
            return null;
        }
        // check that they are of the same type
        if (authenticated.getClass() != databaseSafe.getClass()) {
            log.log(TAG, "Login failed for " + authenticated.readIdentification() + " due to wrong user type!");
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
        Data updateCheck = database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                if (session.update(databaseSafe)) {
                    return new Success("");
                }
                return new Error(Error.Type.DATABASE, "");
            }
        });
        if (!(updateCheck instanceof Success)) {
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
        actives.add(session, active);
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
            for (Active check : actives.getValues()) {
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
        actives.add(active.getSESSION(), active);
    }

    /**
     * Helper function for centralized read from DB.
     *
     * @return The Authenticated freshly read if available, else null.
     */
    private Authenticated readDB(Authenticated authenticated) {
        DataList data;
        if (authenticated instanceof User) {
            data = database.read(new User(authenticated.readIdentification()));
        } else if (authenticated instanceof PublicDisplay) {
            data = database.read(new PublicDisplay(authenticated.readIdentification(), null, null, 0, 0));
        } else if (authenticated instanceof WifiSensor) {
            data = database.read(new WifiSensor(authenticated.readIdentification(), null, null));
        } else {
            log.error(TAG, "Read from DB failed because of wrong object given!");
            return null;
        }
        if (data == null || data.size() == 0) {
            // this means the object wasn't found
            return null;
        }
        if (data.size() != 1) {
            // this is a DB error!
            log.error(TAG, "Read from DB failed because read object is ambiguous!");
            // return null;
        }
        return (Authenticated) data.get(0);
    }
}
