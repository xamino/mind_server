package de.uulm.mi.mind.logger.anonymizer;

import de.uulm.mi.mind.io.DatabaseManager;
import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

/**
 * @author Tamino Hartmann
 *         Class that maps an object to a unique, random string as long as it is registered.
 */
public class Anonymizer {

    public static final String DISALLOWED = "unknown";
    public static final String TEMPORARY = "temporary";
    private static Anonymizer INSTANCE;
    private final String TAG = "Anonymizer";
    private HashMap<String, String> keyUniqueMap;
    private Random random;
    private Messenger log;

    private Anonymizer() {
        random = new Random();
        keyUniqueMap = new HashMap<>();
        log = Messenger.getInstance();
    }

    public synchronized static Anonymizer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Anonymizer();
        }
        return INSTANCE;
    }

    /**
     * Gets a key for a Data object, creating a new one if one doesn't exist yet. Collisions are possible, if unlikely!
     *
     * @param data The data for which to generate a key.
     * @param <E>  The data type.
     * @return The key.
     */
    public <E extends Saveable> String getKey(final E data) {
        // safety
        if (data == null) {
            log.error(TAG, "WARNING: called with null object!");
            return DISALLOWED;
        }
        // check if user may be logged
        if (data instanceof User && !((User) data).isLog()) {
            return DISALLOWED;
        }
        // check if hash doesn't exist
        if (!keyUniqueMap.containsKey(data.getKey())) {
            log.log(TAG, "Unique not cached, reading / creating from db.");
            // if successful, return new key
            if (makeUnique(data)) {
                // in this case add too
                keyUniqueMap.put(data.getKey(), data.getUnique());
                return data.getUnique();
            }
            // else disallowed
            return DISALLOWED;
        }
        return keyUniqueMap.get(data.getKey());
    }

    /**
     * Method that resets the cache. Used for example when a unique is removed to purge data inconsistencies.
     */
    public void resetCache() {
        keyUniqueMap.clear();
    }

    /**
     * Creates the unique if it doesn't exist. Returns true if a unique existed or now exists, otherwise false.
     *
     * @param data The object for which to create unique.
     * @param <E>  Extends saveable but we don't care any exacter.
     * @return True if unique exists, otherwise false.
     */
    public <E extends Saveable> boolean makeUnique(final E data) {
        // don't do anything if null
        if (data.getKey() == null || data.getKey().isEmpty()) {
            log.error(TAG, "Key is null or empty!");
            return false;
        }
        // read from database
        final E shortOriginal = readData(data, false);
        if (shortOriginal == null) {
            log.error(TAG, "Failed to read object!");
            return false;
        }
        // check if available
        if (shortOriginal.getUnique() == null || shortOriginal.getUnique().isEmpty()) {
            // if not, generate and save new unique
            final String unique = data.getClass().getSimpleName() + "#" + new BigInteger(130, random).toString(32);
            // write to data so side effect is correct
            data.setUnique(unique);
            // update it
            return DatabaseManager.getInstance().open(new Transaction() {
                @Override
                public Data doOperations(Session session) {
                    // we need to read the full one because otherwise we loose data
                    E fullOriginal = readData(shortOriginal, true);
                    if (fullOriginal != null) {
                        fullOriginal.setUnique(unique);
                        if (session.update(fullOriginal)) {
                            // all okay!
                            log.log(TAG, "Created new unique for " + shortOriginal.getKey() + ".");
                            return new Success("");
                        }
                    }
                    // this means something went wrong
                    log.error(TAG, "Update for unique failed!");
                    return new Error(Error.Type.DATABASE, "Update failed!");
                }
            }) instanceof Success;
        }
        return true;
    }

    /**
     * Small database helper function.
     *
     * @param saveable
     * @param deep
     * @return
     */
    private <S extends Saveable> S readData(S saveable, boolean deep) {
        DatabaseManager database = DatabaseManager.getInstance();
        DataList<S> temp = database.read(saveable, (deep ? 10 : 1));
        if (temp == null || temp.size() != 1) {
            return null;
        }
        return temp.get(0);
    }
}
