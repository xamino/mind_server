package de.uulm.mi.mind.objects;

import java.util.ArrayList;

/**
 * Created by Cassio on 25.02.14.
 * <p/>
 * Saved Tamino's sanity on Feb 25, 2014. All hail the DataList!
 */
public class DataList<T extends Data> extends ArrayList<T> implements Data {

    public String toString() {

        Data[] a = this.toArray(new Data[0]);

        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    @Override
    public String getKey() {
        return null;
    }
}
