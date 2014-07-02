package de.uulm.mi.mind.logger.anonymizer;

import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.Authenticated;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Tamino Hartmann
 *         Class that maps an object to a unique, random string as long as it is registered.
 */
public class Anonymizer {

    private static Anonymizer INSTANCE;
    private final String DISALLOWED = "unknown";
    private ArrayList<PointerMap> store;
    private Random random;

    private Anonymizer() {
        random = new Random();
        store = new ArrayList<>();
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
    public <E extends Data> String getKey(E data) {
        // check if user may be logged
        if (data instanceof User && !((User) data).isLog()) {
            return DISALLOWED;
        }
        PointerMap<E> mapped = new PointerMap<>(data);
        // check if exists and if yes get the key
        if (store.contains(mapped)) {
            return store.get(store.indexOf(mapped)).getKey();
        }
        // otherwise add
        mapped.setKey(data.getClass().getSimpleName() + "#" + new BigInteger(130, random).toString(32));
        store.add(mapped);
        return mapped.getKey();
    }

    /**
     * Removes a key if exists for the given object.
     *
     * @param data The object for which to unregister the key.
     * @param <E>  The data type.
     */
    public <E extends Data> void removeKey(E data) {
        store.remove(new PointerMap<>(data));
    }

    /**
     * Special class for storing an object<---->key mapping. Note that equals has been overwritten to give the
     * expected behavior.
     *
     * @param <E> The data type of the pointer.
     */
    private class PointerMap<E> {
        private E pointer;
        private String key;

        private PointerMap(E pointer) {
            this.pointer = pointer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PointerMap that = (PointerMap) o;
            // todo this kind of sucks, how do we do this better?
            // now some logic for the subtypes
            if (pointer.getClass() != that.pointer.getClass()) {
                return false;
            }
            if (pointer instanceof Authenticated && that.pointer instanceof Authenticated) {
                return ((Authenticated) pointer).readIdentification()
                        .equals(((Authenticated) that.pointer).readIdentification());
            } else if (pointer instanceof Saveable && that.pointer instanceof Saveable) {
                return ((Saveable) pointer).getKey().equals(((Saveable) that.pointer).getKey());
            }
            // here we hope that something sensible has been implemented :P
            return pointer.equals(that.pointer);
        }

        @Override
        public int hashCode() {
            return pointer.hashCode();
        }

        public E getPointer() {
            return pointer;
        }

        public void setPointer(E pointer) {
            this.pointer = pointer;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
