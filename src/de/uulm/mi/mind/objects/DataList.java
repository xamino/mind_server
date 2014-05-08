package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

import java.util.ArrayList;

/**
 * Created by Cassio on 25.02.14.
 * <p/>
 * Saved Tamino's sanity on Feb 25, 2014. All hail the DataList!
 */
public class DataList<T extends Data> extends ArrayList<T> implements Sendable {

    // Override to allow contains() to work with Data keys.
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            super.indexOf(o);
        } else if (o instanceof Saveable) {
            Saveable d = (Saveable) o;
            if (d.getKey() == null) return super.indexOf(o);
            for (int i = 0; i < size(); i++)
                if (d.getKey().equals(((Saveable) get(i)).getKey()))
                    return i;
        }
        return -1;
    }
}
