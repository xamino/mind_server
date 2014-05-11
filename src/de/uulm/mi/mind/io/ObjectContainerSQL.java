package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Cassio on 08.05.2014.
 */
public class ObjectContainerSQL {

    private final Connection con;

    public ObjectContainerSQL(Connection connection) {
        con = connection;
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void store(Object o) {

    }

    public void delete(Object o) {

    }

    public void commit() {
        try {
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Query query() {
        return new Query();
    }

    public <E extends Data> List<E> query(Predicate<E> predicate) {
        Query q = new Query();
        q.constrain(predicate.getClassType());
        List<E> list = q.execute();

        for (int i = list.size() - 1; i >= 0; i--) {
            if (!predicate.match(list.get(i))) {
                list.remove(i);
            }
        }

        return list;
    }

    public <E extends Data> List queryByExample(E requestFilter) {
        Query q = new Query();
        q.constrain(requestFilter.getClass());

        for (Field field : requestFilter.getClass().getDeclaredFields()) {
            try {
                q.descendConstrain(field.getName(), field.get(requestFilter));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return q.execute();
    }
}
