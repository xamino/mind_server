package de.uulm.mi.mind.objects;

import java.util.ArrayList;

/**
 * Created by Cassio on 25.02.14.
 * <p/>
 * Saved Tamino's sanity on Feb 25, 2014. All hail the DataList!
 */
public class DataList<T extends Data> extends ArrayList<T> implements Data {

    // Override to allow contains() to work with Data keys.
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            super.indexOf(o);
        } else if (o instanceof Data) {
            Data d = (Data) o;
            if (d.getKey() == null) return super.indexOf(o);
            for (int i = 0; i < size(); i++)
                if (d.getKey().equals(get(i).getKey()))
                    return i;
        }
        return -1;
    }


    @Override
    public String getKey() {
        return null;
    }
}
