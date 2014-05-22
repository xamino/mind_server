package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Saveable;

import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
class DatabaseControllerSQL extends DatabaseAccess {
    /**
     * Variable for storing the instance of the class.
     */
    private static DatabaseControllerSQL instance;
    private final String TAG = "DatabaseControllerSQL";

    /**
     * Private constructor for DatabaseController for implementing the singleton
     * instance. Use getInstance() to get a reference to an object of this type.
     */
    private DatabaseControllerSQL() {
        instance = this;
    }

    /**
     * Method for getting a valid reference of this object.
     *
     * @return Instance of DatabaseController.
     */
    public static DatabaseControllerSQL getInstance() {
        if (instance == null) {
            instance = new DatabaseControllerSQL();
        }
        return instance;
    }

    /**
     * Store a Data object in the Database.
     *
     * @param data The Object to be stored.
     * @return true if the operation was successful and false otherwise.
     */
    public boolean create(Session session, Saveable data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        // Sanitize input on DB, only allow Data objects implementing a unique key
        if (data == null || data.getKey() == null) {
            return false;
        }
        // avoid duplicates by checking if there is already a result in DB
        DataList<Saveable> readData = read(session, data);
        if (readData != null && !readData.isEmpty()) {
            return false;
        }
        try {
            sessionContainer.store(data);

            log.log(TAG, "Written to DB: " + data.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads an object from the database.
     *
     * @param requestFilter Filter of objects to be returned. An object constructed with default constructor (null,0 or false values) returns all instances.
     *                      Changing an object parameter filters the returned objects.
     * @return A DataList of the specified filter parameter Type or null on error.
     */
    public <E extends Saveable> DataList<E> read(Session session, final E requestFilter) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        try {
            List queryResult;
            // When unique key is empty, directly use the filter.
            if (requestFilter == null
                    || requestFilter.getKey() == null) { //TODO better location key
                queryResult = sessionContainer.queryByExample(requestFilter);
            } else {
                Query query = sessionContainer.query();
                query.constrain(requestFilter.getClass());
                if (requestFilter instanceof User) {
                    query.descendConstrain("email", requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof Area) {
                    query.descendConstrain("ID", requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof PublicDisplay) {
                    query.descendConstrain("identification", requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof WifiSensor) {
                    query.descendConstrain("identification", requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof Location) {
                    query.descendConstrain("key", requestFilter.getKey());
                    queryResult = query.execute();
                } else {
                    log.log(TAG, "Object Type " + requestFilter.getClass().getSimpleName() + " reading could be optimized.");
                    queryResult = sessionContainer.query(new Predicate<Saveable>(requestFilter.getClass()) {
                        @Override
                        public boolean match(Saveable o) {
                            return o.getKey().equals(requestFilter.getKey());
                        }
                    });
                }
            }

            // Write query results to DataList
            DataList<E> result = new DataList<>();
            if (queryResult != null) {
                for (Object o : queryResult) {
                    result.add((E) o);
                }
            }
            // log.error(TAG, "Read from DB: " + result.toString());
            return result;

        } catch (Exception e) {
            return null;
        }
    }

    public boolean update(Session session, Saveable data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        if (data == null || data.getKey() == null || data.getKey().equals("")) return false;
        try {
            DataList<Saveable> dataList;
            if (!(dataList = read(session, data)).isEmpty()) {
                sessionContainer.delete(dataList.get(0));
                sessionContainer.store(data);
                log.log(TAG, "Updated in DB: " + data.toString());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Deletes an object of type data from database.
     *
     * @param data the object to be deleted.
     * @return true if deletion was successful or the object does not exist, otherwise false
     */
    public boolean delete(Session session, Saveable data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        try {
            DataList<Saveable> dataList = read(session, data);

            // If the data isn't in the DB, the deletion wasn't required, but as the data isn't here, we return true.
            if (dataList == null) {
                return false;
            } else if (data != null && data.getKey() == null && dataList.isEmpty()) { // removal of multiple
                return true;
            } else if (data != null && data.getKey() != null && dataList.isEmpty()) { // removal of specific instance
                return false;
            } else {
                int size = dataList.size();
                for (Saveable d : dataList) {
                    sessionContainer.delete(d);
                }
                log.log(TAG, "Deleted " + size + " objects from DB: " + dataList.toString());
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns a new Database connection
     *
     * @return New MySQL Database Connection
     * @throws SQLException
     */
    Connection createConnection() {
        Configuration config = Configuration.getInstance();

        String url = "jdbc:mysql://" + config.getDbURL() + ":" + config.getDbPort();
        String driver = "com.mysql.jdbc.Driver";
        String user = config.getDbUser();
        String pass = config.getDbPassword();

        Connection con;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            log.log(TAG, "Driver Start failure");
        }

        try {
            con = DriverManager.getConnection(url + "/" + config.getDbName(), user, pass);
        } catch (SQLException e) {
            // possible DB not existant
            log.log(TAG, "DB '" + config.getDbName() + "' probably not found. Create!");
            con = createDatabase(driver, url, config.getDbName(), user, pass);
        }

        //log.log(TAG, "Connection to SQL DB successful.");
        try {
            con.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    private Connection createDatabase(String driver, String url, String dbName, String user, String pass) {
        Connection con = null;
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, pass);
            Statement s = con.createStatement();
            int myResult = s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);

            log.log(TAG, "DB Create/Update Result: " + myResult);

        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            log.log(TAG, "Connection Failure");
            e.printStackTrace();
        }
        return con;
    }


    @Override
    public Session open() {
        return new Session(new ObjectContainerSQL(createConnection()), getInstance());
    }

    @Override
    public void init(ServletContextEvent event) {
        log = Messenger.getInstance();
    }

    @Override
    public void destroy(ServletContextEvent event) {
    }
}
