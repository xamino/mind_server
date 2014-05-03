package de.uulm.mi.mind.io;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.security.BCrypt;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
public class DatabaseController implements ServletContextListener {
    /**
     * Variable for storing the instance of the class.
     */
    private static DatabaseController instance;
    private final String TAG = "DatabaseController";
    private ObjectContainer rootContainer;
    private Messenger log;

    /**
     * Private constructor for DatabaseController for implementing the singleton
     * instance. Use getInstance() to get a reference to an object of this type.
     */
    public DatabaseController() {
        instance = this;
    }

    /**
     * Method for getting a valid reference of this object.
     *
     * @return Instance of DatabaseController.
     */
    public static DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    /**
     * Store a Data object in the Database.
     *
     * @param data The Object to be stored.
     * @return true if the operation was successful and false otherwise.
     */
    public boolean create(ObjectContainer sessionContainer, Data data) {
        // Sanitize input on DB, only allow Data objects implementing a unique key
        if (data == null || data.getKey() == null) {
            return false;
        }
        // avoid duplicates by checking if there is already a result in DB
        DataList<Data> readData = read(sessionContainer, data);
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
     * @param <T>           A DataType extending Data.
     * @return A DataList of the specified filter parameter Type or null on error.
     */
    public <T extends Data> DataList<T> read(ObjectContainer sessionContainer, final T requestFilter) {
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
                    query.descend("email").constrain(requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof Area) {
                    query.descend("ID").constrain(requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof PublicDisplay) {
                    query.descend("identification").constrain(requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof WifiSensor) {
                    query.descend("identification").constrain(requestFilter.getKey());
                    queryResult = query.execute();
                } else if (requestFilter instanceof Location) {
                    query.descend("key").constrain(requestFilter.getKey());
                    queryResult = query.execute();
                } else {
                    log.log(TAG, "Object Type " + requestFilter.getClass().getSimpleName() + " reading could be optimized.");
                    queryResult = sessionContainer.query(new Predicate<T>() {
                        @Override
                        public boolean match(T o) {
                            return o.getKey().equals(requestFilter.getKey());
                        }
                    });
                }
            }

            // Write query results to DataList
            DataList<T> result = new DataList<>();
            if (queryResult != null) {
                for (Object o : queryResult) {
                    result.add((T) o);
                }
            }
            // log.error(TAG, "Read from DB: " + result.toString());
            return result;

        } catch (Exception e) {
            return null;
        }
    }

    // todo look if we can use this
    public DataList<Area> getAreasContainingLocation(ObjectContainer sessionContainer, final Location contained) {
        List queryResult = sessionContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area match) {
                // return all areas that include the single location contained in the requestfilter
                double x = contained.getCoordinateX();
                double y = contained.getCoordinateY();
                return match.contains(x, y);
            }
        });
        // Write query results to DataList
        DataList<Area> result = new DataList<>();
        if (queryResult != null) {
            for (Object o : queryResult) {
                result.add((Area) o);
            }
        }
        return result;
    }

    public boolean update(ObjectContainer sessionContainer, Data data) {
        if (data == null || data.getKey() == null || data.getKey().equals("")) return false;
        try {
            DataList<Data> dataList;
            if (!(dataList = read(sessionContainer, data)).isEmpty()) {
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
    public boolean delete(ObjectContainer sessionContainer, Data data) {
        try {
            DataList<Data> dataList = read(sessionContainer, data);

            // If the data isn't in the DB, the deletion wasn't required, but as the data isn't here, we return true.
            if (dataList == null) {
                return false;
            } else if (data != null && data.getKey() == null && dataList.isEmpty()) { // removal of multiple
                return true;
            } else if (data != null && data.getKey() != null && dataList.isEmpty()) { // removal of specific instance
                return false;
            } else {
                for (Data d : dataList) {
                    sessionContainer.delete(d);
                }
                log.log(TAG, "Deleted from DB: " + dataList.toString());
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public ObjectContainer getSessionContainer() {
        // open the db4o-session.
        return rootContainer.ext().openSession();
    }

    public void init(String servletFilePath, boolean reinitialize) {
        Configuration config = Configuration.getInstance();
        config.init(servletFilePath); // must be first!!!
        log = Messenger.getInstance();
        String dbFilePath = servletFilePath + "WEB-INF/" + config.getDbName();

        EmbeddedConfiguration dbconfig = Db4oEmbedded.newConfiguration();
        //dbconfig.common().diagnostic().addListener(new DiagnosticToConsole());
        dbconfig.common().objectClass(Area.class).cascadeOnUpdate(true);
        dbconfig.common().objectClass(Location.class).cascadeOnUpdate(true);
        //dbconfig.common().optimizeNativeQueries(true);
        dbconfig.common().objectClass(User.class).objectField("email").indexed(true);
        dbconfig.common().objectClass(Area.class).objectField("ID").indexed(true);
        dbconfig.common().objectClass(PublicDisplay.class).objectField("identification").indexed(true);
        dbconfig.common().objectClass(WifiSensor.class).objectField("identification").indexed(true);
        dbconfig.common().objectClass(Location.class).objectField("key").indexed(true);
        rootContainer = Db4oEmbedded.openFile(dbconfig, dbFilePath);

        log.log(TAG, "db4o startup on " + dbFilePath);

        /*ObjectSet<Location> locs = rootContainer.query(Location.class);
        for (Location loc : locs){
            loc.setCoordinateX(loc.getCoordinateX());
            rootContainer.store(loc);
        }*/

        if (reinitialize) {
            reinit(rootContainer);
            rootContainer.commit();
        }

    }

    /**
     * Must not be called before the
     */
    public void reinit(ObjectContainer sessionContainer) {
        //
        Configuration config = Configuration.getInstance();

        // Initializing Database
        log.log(TAG, "Running DB init.");
        DataList<Area> areaData = read(sessionContainer, new Area("University", null, 0, 0, 0, 0));
        if (areaData == null || areaData.isEmpty()) {
            log.log(TAG, "Universe not existing, creating it.");
            create(sessionContainer, new Area("University", new DataList<Location>(), 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        // Create default admin if no other admin exists
        User adminProto = new User(null);
        adminProto.setAdmin(true);
        DataList<User> adminData = read(sessionContainer, adminProto);
        // test for existing single admin or list of admins
        if (adminData == null || adminData.isEmpty()) {
            log.log(TAG, "Admin not existing, creating one.");
            adminProto = new User(config.getAdminEmail(), config.getAdminName(), true);
            adminProto.setPwdHash(BCrypt.hashpw(config.getAdminPassword(), BCrypt.gensalt(12)));
            create(sessionContainer, adminProto);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String filePath = context.getRealPath("/");
        init(filePath, true);
    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {
        close();
    }

    public void close() {
        if (rootContainer != null) {
            rootContainer.close();
            log.log(TAG, "db4o shutdown");
        }
    }
}
