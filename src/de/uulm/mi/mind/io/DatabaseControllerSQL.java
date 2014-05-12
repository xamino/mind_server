package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.security.BCrypt;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
public class DatabaseControllerSQL implements DatabaseAccess {
    /**
     * Variable for storing the instance of the class.
     */
    private static DatabaseControllerSQL instance;
    private final String TAG = "DatabaseController";
    private ObjectContainerSQL rootContainer;
    private Messenger log;

    /**
     * Private constructor for DatabaseController for implementing the singleton
     * instance. Use getInstance() to get a reference to an object of this type.
     */
    public DatabaseControllerSQL() {
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
    public boolean create(Session session, Data data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        // Sanitize input on DB, only allow Data objects implementing a unique key
        if (data == null || data.getKey() == null) {
            return false;
        }
        // avoid duplicates by checking if there is already a result in DB
        DataList<Data> readData = read(session, data);
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
    public <E extends Data> DataList<E> read(Session session, final E requestFilter) {
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
                    queryResult = sessionContainer.query(new Predicate<Data>(requestFilter.getClass()) {
                        @Override
                        public boolean match(Data o) {
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

    public boolean update(Session session, Data data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        if (data == null || data.getKey() == null || data.getKey().equals("")) return false;
        try {
            DataList<Data> dataList;
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
    public boolean delete(Session session, Data data) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        try {
            DataList<Data> dataList = read(session, data);

            // If the data isn't in the DB, the deletion wasn't required, but as the data isn't here, we return true.
            if (dataList == null) {
                return false;
            } else if (data != null && data.getKey() == null && dataList.isEmpty()) { // removal of multiple
                return true;
            } else if (data != null && data.getKey() != null && dataList.isEmpty()) { // removal of specific instance
                return false;
            } else {
                int size = dataList.size();
                for (Data d : dataList) {
                    sessionContainer.delete(d);
                }
                log.log(TAG, "Deleted " + size + " objects from DB: " + dataList.toString());
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public ObjectContainerSQL getSessionContainer() {
        // open the db4o-session.
        return new ObjectContainerSQL(createConnection());
    }

    public void init(String servletFilePath, boolean reinitialize) {
        Configuration config = Configuration.getInstance();
        config.init(servletFilePath); // must be first!!!
        log = Messenger.getInstance();

        //rootContainer = getSessionContainer();


        //runMaintenance(rootContainer);

        if (reinitialize) {
            //reinit(rootContainer);
            //rootContainer.commit();
        }

    }

    private void runMaintenance(ObjectContainerSQL rootContainer) {
        log.log(TAG, "In DB Stats:");

        List<Area> set2 = rootContainer.query(new Predicate<Area>(Area.class) {
            @Override
            public boolean match(Area o) {
                return true;
            }
        });
        log.log(TAG, "Areas: " + set2.size());
        List<Location> set1 = rootContainer.query(new Predicate<Location>(Location.class) {
            @Override
            public boolean match(Location o) {
                return true;
            }
        });
        log.log(TAG, "Locations: " + set1.size());
        List<WifiMorsel> set = rootContainer.query(new Predicate<WifiMorsel>(WifiMorsel.class) {
            @Override
            public boolean match(WifiMorsel o) {
                return true;
            }
        });
        log.log(TAG, "Morsels: " + set.size());

        log.log(TAG, "In Universe:");
        List<Area> set3 = rootContainer.query(new Predicate<Area>(Area.class) {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });
        Area university = set3.get(0);
        log.log(TAG, "Locations: " + university.getLocations().size());
        int morselCounter = 0;
        for (Location location : university.getLocations()) {
            morselCounter += location.getWifiMorsels().size();
        }
        log.log(TAG, "Morsels: " + morselCounter);
    }

    /**
     * Must not be called before the
     */
    public void reinit(Session session) {
        ObjectContainerSQL sessionContainer = session.getSqlContainer();
        //
        Configuration config = Configuration.getInstance();

        // Initializing Database
        log.log(TAG, "Running DB init.");
        DataList<Area> areaData = read(session, new Area("University", null, 0, 0, 0, 0));
        if (areaData == null || areaData.isEmpty()) {
            log.log(TAG, "Universe not existing, creating it.");
            create(session, new Area("University", new DataList<Location>(), 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        // Create default admin if no other admin exists
        User adminProto = new User(null);
        adminProto.setAdmin(true);
        DataList<User> adminData = read(session, adminProto);
        // test for existing single admin or list of admins
        if (adminData == null || adminData.isEmpty()) {
            log.log(TAG, "Admin not existing, creating one.");
            adminProto = new User(config.getAdminEmail(), config.getAdminName(), true);
            adminProto.setPwdHash(BCrypt.hashpw(config.getAdminPassword(), BCrypt.gensalt(12)));
            create(session, adminProto);
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

        Connection con = null;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            log.log(TAG, "Driver Start failure");
        }

        try {
            con = DriverManager.getConnection(url + "/" + config.getDbName(), user, pass);
        } catch (SQLException e) {
            // possible DB not existant
            con = createDatabase(driver, url, config.getDbName(), user, pass);
            log.log(TAG, "Connection Failure");
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
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
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
        ServletContext context = event.getServletContext();
        String filePath = context.getRealPath("/");
        init(filePath, true);
    }

    @Override
    public void destroy(ServletContextEvent event) {
        if (rootContainer != null) {
            rootContainer.close();
            log.log(TAG, "db4o shutdown");
        }
    }
}
