package database.objects;

import database.Data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Cassio on 25.02.14.
 */
public class DataList extends ArrayList<Data> implements Data {
    public String toString(){

        Data[] a = this.toArray(new Data[]{});

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
}
