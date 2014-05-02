package de.uulm.mi.mind.objects.unsendable;

import java.util.*;

/**
 * @author Tamino Hartmann
 *         A class that stores values until the specified timeout has passed for them. Objects are kept as long as they
 *         are updated or added anew.
 */
public class TimedQueue<E, F> {

    /**
     * The timeout after which objects are discarded.
     */
    private final long TIMEOUT;
    /**
     * Set for unique keys.
     */
    private Set<E> keys;
    /**
     * Stores the associated objects.
     */
    private HashMap<E, F> objects;
    /**
     * Stores the time for each object.
     */
    private HashMap<E, Long> time;

    /**
     * Constructor with timeout specified.
     *
     * @param timeout Final value for timeout in milliseconds.
     */
    public TimedQueue(long timeout) {
        TIMEOUT = timeout;
        keys = new HashSet<>();
        objects = new HashMap<>();
        time = new HashMap<>();
    }

    /**
     * Add a set.
     *
     * @param key    The key under which to add the entry.
     * @param object The corresponding value.
     */
    public void add(E key, F object) {
        long currentTime = System.currentTimeMillis();
        keys.add(key);
        objects.put(key, object);
        time.put(key, currentTime);
    }

    /**
     * Removes a key value pair no matter if it still exists.
     *
     * @param key The key to remove.
     */
    public void remove(E key) {
        keys.remove(key);
        objects.remove(key);
        time.remove(key);
    }

    /**
     * Reads a value for a key. Does not update the time!
     *
     * @param key The key to read.
     * @return The corresponding object – can be null if none found.
     */
    public F get(E key) {
        maintain();
        return objects.get(key);
    }

    /**
     * Method for checking if the queue has an entry for a key.
     *
     * @param key The key to check.
     * @return Whether an entry exists.
     */
    public boolean contains(E key) {
        maintain();
        return keys.contains(key);
    }

    /**
     * Method for trimming off old key:value pairs.
     */
    private void maintain() {
        long currentTime = System.currentTimeMillis();
        ArrayList<E> keyRemove = new ArrayList<>();
        for (E key : keys) {
            if (time.get(key) <= currentTime - TIMEOUT) {
                keyRemove.add(key);
            }
        }
        for (E key : keyRemove) {
            this.remove(key);
        }
    }

    /**
     * The number of elements currently active.
     *
     * @return The size.
     */
    public int getSize() {
        maintain();
        return keys.size();
    }

    /**
     * Method that returns the timestamp for a key.
     *
     * @param key The key to check.
     * @return The time in long that the key was entered.
     */
    public long getTime(E key) {
        return time.get(key);
    }

    public Collection<F> getValues() {
        return (Collection) objects.values();
    }
}