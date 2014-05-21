package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Interfaces.Saveable;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Cassio on 08.05.2014.
 */
class ObjectContainerSQL {
    private static final String TAG = "ObjectContainerSQL";
    private static final String BOOLEAN = "BOOLEAN";
    private static final String INTEGER = "INZT";
    private static final String VARCHAR = "VARCHAR(255)";
    private static final String DATE = "DATETIME";
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

        // Build table structure
        createTableForClassIfNotExist(o);

        // insert values
        String columnQuery = "";
        String valueQuery = "";
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                // get SQL type of field
                Class<?> type = field.getType();
                // escape strings in query
                Object val = field.get(o);
                if (val == null) {
                    valueQuery += null + ",";
                } else if (type == String.class || type.isEnum()) {
                    valueQuery += "'" + val + "',";
                } else if (val instanceof Date) {
                    valueQuery += "'" + new Timestamp(((Date) val).getTime()) + "',";
                } else {
                    valueQuery += val + ",";
                }

                columnQuery += field.getName() + ",";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        columnQuery = columnQuery.substring(0, columnQuery.lastIndexOf(","));
        valueQuery = valueQuery.substring(0, valueQuery.lastIndexOf(","));


        String query = "INSERT INTO " + tableName + " (" + columnQuery + ") VALUES (" + valueQuery + ")";

        log.log(TAG, query);

        try {
            //insert values
            PreparedStatement pstm = con.prepareStatement(query);
            pstm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createTableForClassIfNotExist(Object o) {
        String tableName = o.getClass().getSimpleName();
        int size = -1;
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SHOW TABLES LIKE '" + tableName + "'");
            rs.last();
            size = rs.getRow();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            // exists already
            return;
        }

        String columnTypes = "";
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            // get SQL type of field
            String type = classToSQLType(field.getType());
            columnTypes += field.getName() + " " + type + ",";
        }
        columnTypes = columnTypes.substring(0, columnTypes.lastIndexOf(","));

        String tableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + columnTypes + ")";
        log.log(TAG, tableQuery);
        try {
            //create table structure
            PreparedStatement statement = con.prepareStatement(tableQuery);
            statement.execute();
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

    public <E extends Saveable> List<E> query(Predicate<E> predicate) {
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

    public <E extends Saveable> List<E> queryByExample(E requestFilter) {
        Query q = new Query();
        q.constrain(requestFilter.getClass());

        for (Field field : requestFilter.getClass().getDeclaredFields()) {
            field.setAccessible(true);
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
