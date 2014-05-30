package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by Cassio on 08.05.2014.
 */
class Query {

    private final Messenger log;
    private final Connection connection;
    private Class<?> objectClass;
    private final HashMap<Object, Object> conditionMap;
    private static final String TAG = "Query";

    public Query(Connection connection) {
        this.connection = connection;
        log = Messenger.getInstance();
        conditionMap = new HashMap<>();
    }

    public Query descendConstrain(Object o, Object c) {
        if (c == null
                || (c instanceof Boolean && !((boolean) c))
                || (c instanceof Integer && (int) c == 0)
                || c == 0.0)
            return this; // Removes condition allowing all matching results in these cases TODO does this wrong?
        conditionMap.put(o, c);
        return this;
    }

    public <E> ArrayList<E> execute() {
        ArrayList<E> list = new ArrayList<>();
        try {
            String query = "SELECT * FROM " + objectClass.getCanonicalName().replace(".", "_");

            if (conditionMap.size() > 0) {
                query += " WHERE ";
                for (Map.Entry<Object, Object> objectObjectEntry : conditionMap.entrySet()) {
                    Object val = objectObjectEntry.getValue();
                    if (val instanceof String || val instanceof Enum) {
                        val = "'" + val + "'";
                    }
                    query += objectObjectEntry.getKey() + " = " + val + " AND ";
                }
                query = query.substring(0, query.lastIndexOf("AND") - 1);
            }

            log.log(TAG, query);

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                list.add(this.<E>rowToObject(objectClass, rs, connection));
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            log.error(TAG, e.getMessage());
        }
        return list;
    }

    private <E> E rowToObject(Class<?> objectClass, ResultSet rs, Connection connection) throws SQLException {
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

            Object parsedValue = typeCastParse(f.getType(), f.getName(), rs, objectClass, connection, f);
            try {
                // this is where we also parse the value to the correct type
                f.set(object, parsedValue);
            } catch (IllegalAccessException e) {
                //throw new IOException(TAG + ": Failed to write fields!");
            }

        }
        return object;
    }

    private Object typeCastParse(Class<?> type, String column, ResultSet rs, Class<?> parentType, Connection connection, Field f) throws SQLException {
        if (type == boolean.class) {
            return rs.getBoolean(column);
        } else if (type == byte.class) {
            return rs.getByte(column);
        } else if (type == short.class) {
            return rs.getShort(column);
        } else if (type == int.class) {
            return rs.getInt(column);
        } else if (type == long.class) {
            return rs.getLong(column);
        } else if (type == float.class) {
            return rs.getFloat(column);
        } else if (type == double.class) {
            return rs.getDouble(column);
        } else if (type == String.class) {
            return rs.getString(column);
        } else if (type == Date.class) {
            Timestamp date = rs.getTimestamp(column);
            if (date == null) return null;
            return new Date(date.getTime());
        } else if (type.isEnum()) {
            String enumString = rs.getString(column);
            if (enumString == null) return null;
            return Enum.valueOf((Class) type, enumString);
        } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return getObjectList(parentType, ObjectContainerSQL.getClassFromGenericField(f), rs.getInt("aid"), connection);
        } else
            return null;
    }

    private <E> Collection<E> getObjectList(Class<?> parentType, Class<? extends E> type, int aid, Connection connection) {
        String element = type.getCanonicalName().replace(".", "_");
        String oneToMany = parentType.getSimpleName() + "__" + type.getSimpleName();

        String query = "SELECT * FROM "
                + oneToMany
                + " LEFT JOIN " + element
                + " ON " + oneToMany + ".cid = " + element + ".aid"
                + " WHERE " + oneToMany + ".pid = " + aid;

        log.log(TAG, query);

        Collection<E> objects = new DataList(); //TODO get rid
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                objects.add(this.<E>rowToObject(type, rs, connection));
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return objects;
    }

    public void constrain(Class<?> aClass) {
        objectClass = aClass;
    }
}
