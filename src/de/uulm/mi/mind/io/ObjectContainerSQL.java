package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by Cassio on 08.05.2014.
 */
class ObjectContainerSQL {
    private static final String TAG = "ObjectContainerSQL";
    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "int";
    private static final String VARCHAR = "varchar(255)";
    private static final String DATE = "date";
    private final Messenger log;
    private final Connection con;


    public ObjectContainerSQL(Connection connection) {
        con = connection;
        log = Messenger.getInstance();
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void store(Object o) {

        String tableName = o.getClass().getSimpleName();

        String columnTypes = "";
        String columnQuery = "";
        String valueQuery = "";
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {

                // get SQL type of field
                String type = classToSQLType(field.getType());

                // escape varchars
                Object val = field.get(o);
                if (val != null && (type.equals(VARCHAR) || type.startsWith("ENUM"))) {
                    valueQuery += "'" + val + "',";
                } else {
                    valueQuery += "" + val + ",";
                }

                columnQuery += field.getName() + ",";
                columnTypes += field.getName() + " " + type + ",";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        columnQuery = columnQuery.substring(0, columnQuery.lastIndexOf(","));
        valueQuery = valueQuery.substring(0, valueQuery.lastIndexOf(","));
        columnTypes = columnTypes.substring(0, columnTypes.lastIndexOf(","));

        String tableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + columnTypes + ")";
        String query = "INSERT INTO " + tableName + " (" + columnQuery + ") VALUES (" + valueQuery + ")";
        log.log(TAG, tableQuery);
        log.log(TAG, query);

        try {
            PreparedStatement statement = con.prepareStatement(tableQuery);
            statement.execute();

            PreparedStatement pstm = con.prepareStatement(query);
            pstm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private String classToSQLType(Class<?> type) {
        if (type == boolean.class) {
            return BOOLEAN;
        } else if (type == int.class) {
            return INTEGER;
        } else if (type == String.class) {
            return VARCHAR;
        } else if (type == Date.class) {
            return DATE;
        } else if (type.isEnum()) {
            String enumString = "ENUM(";
            Object[] constants = type.getEnumConstants();
            for (Object constant : constants) {
                enumString += "'" + constant.toString() + "',";
            }
            enumString = enumString.substring(0, enumString.lastIndexOf(","));
            enumString += ")";
            return enumString;
        }
        //TODO
        return VARCHAR;

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

    public void rollback() {

    }
}
