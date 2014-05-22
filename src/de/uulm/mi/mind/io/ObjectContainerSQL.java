package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by Cassio on 08.05.2014.
 */
class ObjectContainerSQL {
    private static final String TAG = "ObjectContainerSQL";
    private static final String BOOLEAN = "BOOLEAN";
    private static final String BYTE = "TINYINT";
    private static final String SHORT = "SMALLINT";
    private static final String INT = "INTEGER";
    private static final String LONG = "BIGINT";
    private static final String FLOAT = "FLOAT";
    private static final String DOUBLE = "DOUBLE";
    private static final String VARCHAR = "VARCHAR(255)";
    private static final String DATETIME = "DATETIME";

    private static final String COLESC = "`";
    private static final String STRESC = "'";

    private static final String OBJECT = "OBJECT";
    private static final String LIST = "LIST";
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

    public int store(Object o) {
        String tableName = o.getClass().getCanonicalName().replace('.', '_');

        // Build table structure
        createTableForClassIfNotExist(o.getClass());

        // insert values
        String columnQuery = "";
        String valueQuery = "";

        HashMap<Class<?>, Collection> objectArrayList = new HashMap<>();

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
                    valueQuery += STRESC + val + STRESC + ",";
                } else if (type == Date.class) {
                    valueQuery += STRESC + new Timestamp(((Date) val).getTime()) + STRESC + ",";
                } else if (Collection.class.isAssignableFrom(type) || type.isArray()) {
                    objectArrayList.put(getClassFromGenericField(field), (Collection) val);
                    continue;
                } else {
                    valueQuery += val + ",";
                }

                columnQuery += COLESC + field.getName() + COLESC + ",";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        columnQuery = columnQuery.substring(0, columnQuery.lastIndexOf(","));
        valueQuery = valueQuery.substring(0, valueQuery.lastIndexOf(","));


        String query = "INSERT INTO " + tableName + " (" + columnQuery + ") VALUES (" + valueQuery + ")";

        log.log(TAG, query);

        int insertID = -1;
        try {
            //insert values
            PreparedStatement pstm = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                insertID = rs.getInt(1);
            }
            rs.close();
            pstm.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Class<?>, Collection> entry : objectArrayList.entrySet()) {
            storeCollection(o.getClass(), entry.getKey(), entry.getValue(), insertID);
        }

        return insertID;

    }

    private <E> void storeCollection(Class<?> containerClass, Class<? extends E> elementClass, Collection<E> children, int pid) {

        String container = containerClass.getCanonicalName().replace(".", "_");
        String element = elementClass.getCanonicalName().replace(".", "_");
        String oneToMany = containerClass.getSimpleName() + "__" + elementClass.getSimpleName();

        ArrayList<Integer> cids = new ArrayList<>();

        //store children
        for (E child : children) {
            cids.add(store(child));
        }
        // update relation table
        String query = "INSERT INTO " + oneToMany + " (pid,cid) VALUES (?,?)";
        try {
            PreparedStatement pstm = con.prepareStatement(query);

            for (Integer cid : cids) {
                pstm.setInt(1, pid);
                pstm.setInt(2, cid);
                pstm.executeUpdate();
            }
            pstm.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void createTableForClassIfNotExist(Class<?> o) {
        String tableName = o.getCanonicalName().replace('.', '_');

        log.log(TAG, "Create Table for: " + o.getSimpleName());

        // Check if table already exists
        int size = -1;
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SHOW TABLES LIKE " + STRESC + tableName + STRESC);
            rs.last();
            size = rs.getRow();
            rs.close();
            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            // exists already
            return;
        }

        // Create new table
        // give it a auto increment index column for referencing
        String columnTypes = "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,";
        ArrayList<String> foreignKeys = new ArrayList<>();
        for (Field field : o.getDeclaredFields()) {
            field.setAccessible(true);

            Class<?> fieldClass = field.getType();

            // get SQL type of field
            String type = classToSQLType(fieldClass);
            if (type.equals(OBJECT)) {
                log.log(TAG, "Nested class found: " + fieldClass.getSimpleName());
                createTableForClassIfNotExist(fieldClass);
                //foreignKeys.add("FOREIGN KEY (" + COLESC + field.getName() + COLESC + ") REFERENCES " + fieldClass.getCanonicalName().replace(".", "_") + "(id),");
                continue;
            } else if (type.equals(LIST)) {
                Class<?> elementClass = getClassFromGenericField(field);
                log.log(TAG, "Nested list/array found: " + fieldClass.getSimpleName() + " of " + elementClass.getSimpleName());
                createTableForClassIfNotExist(elementClass);
                createTableForListsIfNotExist(o, elementClass);
                //foreignKeys.add("FOREIGN KEY (" + COLESC + field.getName() + COLESC + ") REFERENCES " + o.getSimpleName() + "__" + elementClass.getSimpleName() + "(pid),");
                continue;
            }
            columnTypes += COLESC + field.getName() + COLESC + " " + type + ",";
        }

        for (String foreignKey : foreignKeys) {
            columnTypes += foreignKey;
        }

        columnTypes = columnTypes.substring(0, columnTypes.lastIndexOf(","));

        String tableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnTypes + ")";
        log.log(TAG, tableQuery);
        try {
            //create table structure
            PreparedStatement statement = con.prepareStatement(tableQuery);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableForListsIfNotExist(Class<?> containerClass, Class<?> elementClass) {
        String tableQuery = "CREATE TABLE IF NOT EXISTS " +
                containerClass.getSimpleName() + "__" + elementClass.getSimpleName() +
                " (pid INTEGER NOT NULL," +
                " cid INTEGER NOT NULL," +
                "INDEX pid_index (pid)," +
                //"FOREIGN KEY (" + COLESC + "pid" + COLESC + ") REFERENCES " + containerClass.getCanonicalName().replace(".", "_") + "(id)," +
                "FOREIGN KEY (" + COLESC + "cid" + COLESC + ") REFERENCES " + elementClass.getCanonicalName().replace(".", "_") + "(id))";
        log.log(TAG, tableQuery);

        try {
            //create table structure
            PreparedStatement statement = con.prepareStatement(tableQuery);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String classToSQLType(Class<?> type) {
        if (type == boolean.class) {
            return BOOLEAN;
        } else if (type == byte.class) {
            return BYTE;
        } else if (type == short.class) {
            return SHORT;
        } else if (type == int.class) {
            return INT;
        } else if (type == long.class) {
            return LONG;
        } else if (type == float.class) {
            return FLOAT;
        } else if (type == double.class) {
            return DOUBLE;
        } else if (type == String.class) {
            return VARCHAR;
        } else if (type == Date.class) {
            return DATETIME;
        } else if (type.isEnum()) {
            String enumString = "ENUM(";
            Object[] constants = type.getEnumConstants();
            for (Object constant : constants) {
                enumString += STRESC + constant.toString() + STRESC + ",";
            }
            enumString = enumString.substring(0, enumString.lastIndexOf(","));
            enumString += ")";
            return enumString;
        } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return LIST;
        } else {
            return OBJECT;
        }
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

    public <E> List<E> query(Predicate<E> predicate) {
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

    public <E> List<E> queryByExample(E requestFilter) {
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

    static Class<?> getClassFromGenericField(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    public void rollback() {
        try {
            con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
