package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Cassio on 08.05.2014.
 */
class Query {

    private final Messenger log;
    private String table;
    private HashMap<Object, Object> conditionMap;
    private static final String TAG = "Query";

    public Query() {
        log = Messenger.getInstance();
        conditionMap = new HashMap<>();
    }

    public Query descendConstrain(Object o, Object c) {
        if (c == null || c == 0 || c == 0.0)
            return this; // Removes condition allowing all matching results in these cases TODO does this wrong?
        conditionMap.put(o, c);
        return this;
    }

    public <E extends Data> List<E> execute() {
        List<E> list = new ArrayList<>();
        try {
            Connection connection = DatabaseControllerSQL.getInstance().createConnection();

            String query = "SELECT * FROM " + table;

            if (conditionMap.size() > 0) {
                query += " WHERE ";
                for (Map.Entry<Object, Object> objectObjectEntry : conditionMap.entrySet()) {
                    query += objectObjectEntry.getKey() + " = " + objectObjectEntry.getValue() + " AND ";
                }
                query += "is TRUE";
            }

            log.log(TAG, query);

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                Class<?> objectClass = Class.forName("de.uulm.mi.mind.objects." + table);
                log.log(TAG, "Table2Class: de.uulm.mi.mind.objects." + table);
                E object = null;
                try {
                    Constructor constructor = objectClass.getDeclaredConstructor(new Class[]{});
                    constructor.setAccessible(true);
                    object = (E) constructor.newInstance(new Class[]{});
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    log.error(TAG, "Default constructor missing for " + objectClass.toString() + "!");
                    //throw new IOException(TAG + ": Objects must have default constructor! May be private though.");
                }
                // apply all fields, leaving those we have no value for at their default value
                for (Field f : objectClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object parsedValue = typeCastParse(f.getType(), f.getName(), rs);
                    try {
                        // this is where we also parse the value to the correct type
                        f.set(object, parsedValue);
                    } catch (IllegalAccessException e) {
                        //throw new IOException(TAG + ": Failed to write fields!");
                    }

                }
                list.add(object);
            }
            rs.close();
            connection.close();

        } catch (SQLException e) {
            log.error(TAG, e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Object typeCastParse(Class<?> type, String column, ResultSet rs) throws SQLException {
        if (type == boolean.class) {
            return rs.getBoolean(column);
        } else if (type == int.class) {
            return rs.getInt(column);
        } else if (type == String.class) {
            return rs.getString(column);
        } else if (type == Date.class) {
            if (rs.getDate(column) == null) {
                return null;
            }
            return new Date(rs.getDate(column).getTime());
        } else if (type.isEnum()) {
            String enumString = rs.getString(column);
            if (enumString == null) return enumString;
            return Enum.valueOf((Class) type, enumString);
        } else
            return null;
    }

    public void constrain(Class<? extends Data> aClass) {
        table = aClass.getSimpleName();
    }
}
