package de.uulm.mi.mind.objects.unsendable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Tamino Hartmann
 */
public class TimedQueue<E, F> {
    private final long TIMEOUT;
    private ArrayList<E> keys;
    private HashMap<E, F> objects;
    private HashMap<E, Long> time;

    public TimedQueue(long timeout) {
        TIMEOUT = timeout;
        keys = new ArrayList<>();
        objects = new HashMap<>();
        time = new HashMap<>();
    }

    public void add(E key, F object) {
        long currentTime = System.currentTimeMillis();
        keys.add(key);
        objects.put(key, object);
        time.put(key, currentTime);
    }

    public void remove(E key) {
        keys.remove(key);
        objects.remove(key);
        time.remove(key);
    }

    public F get(E key) {
        maintain();
        return objects.get(key);
    }

    public boolean contains(E key) {
        maintain();
        return keys.contains(key);
    }

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

    public int getSize() {
        maintain();
        return keys.size();
    }
    
    public long getTime(E key) {
        return time.get(key);
    }
}
