package de.uulm.mi.mind.io;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
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
    private ObjectContainer con;
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
    public boolean create(Data data) {
        // Sanitize input on DB, only allow Data objects implementing a unique key
        if (data == null || data.getKey() == null || data.getKey().equals("")) {
            return false;
        }
        // avoid duplicates by checking if there is already a result in DB
        DataList<Data> readData = read(data);
        if (readData != null && !readData.isEmpty()) {
            return false;
        }
        try {
            con.store(data);
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
    public <T extends Data> DataList<T> read(final T requestFilter) {
        try {
            List queryResult;
            // When unique key is empty, directly use the filter.
            if (requestFilter == null
                    || requestFilter.getKey() == null
                    || requestFilter.getKey().equals("")
                    || requestFilter.getKey().equals("0.0/0.0")) { //TODO better location key
                queryResult = con.queryByExample(requestFilter);
            } else {
                queryResult = con.query(new Predicate<T>() {
                    @Override
                    public boolean match(T o) {
                        return o.getKey().equals(requestFilter.getKey());
                    }
                });
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

    public DataList<Area> getAreasContainingLocation(final Location contained) {
        List queryResult = con.query(new Predicate<Area>() {
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

    /**
     * Reads child lists of the specified Data object if any available.
     *
     * @param requestFilter The Data object of which the children should be fetched.
     * @return a Datalist of children or null on error.
     */
    public Data readChildren(Data requestFilter) {
        try {
            List queryResult;
            DataList result = null;

            // process all possible classes
            if (requestFilter instanceof Location) {
                final Location loc = (Location) requestFilter;
                queryResult = con.query(new Predicate<Location>() {
                    @Override
                    public boolean match(Location o) {
                        return o.getCoordinateX() == loc.getCoordinateX() && o.getCoordinateY() == loc.getCoordinateY();
                    }
                });

                if (queryResult.size() > 0) {
                    result = ((Location) queryResult.get(0)).getWifiMorsels();
                }

            } else if (requestFilter instanceof Area) {
                final Area area = (Area) requestFilter;
                queryResult = con.query(new Predicate<Area>() {
                    @Override
                    public boolean match(Area o) {
                        return o.getID().equals(area.getID());
                    }
                });

                if (queryResult.size() > 0) {
                    result = ((Area) queryResult.get(0)).getLocations();
                }
            } else {
                return null;
            }
            if (result == null)
                result = new DataList();

            //log.log(TAG, result.toString() + " read from DB!");
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean update(Data data) {
        if (data == null || data.getKey() == null || data.getKey().equals("")) return false;

        try {
            if (data instanceof User) {
                User dataUser = (User) data;
                User userToUpdate = read(dataUser).get(0); // TODO Empty/NullCheck
                userToUpdate.setName(dataUser.getName());
                userToUpdate.setPwdHash(dataUser.getPwdHash());
                userToUpdate.setAdmin(dataUser.isAdmin());
                userToUpdate.setAccessDate(dataUser.getAccessDate());

                con.store(userToUpdate);

                log.log(TAG, "Updated in DB: " + userToUpdate.toString());

                return true;
            } else if (data instanceof Area) {
                Area dataArea = (Area) data;
                Area areaToUpdate = read(dataArea).get(0); // TODO Empty/NullCheck
                areaToUpdate.setID(dataArea.getID());
                areaToUpdate.setLocations(dataArea.getLocations());
                areaToUpdate.setHeight(dataArea.getHeight());
                areaToUpdate.setWidth(dataArea.getWidth());
                areaToUpdate.setTopLeftX(dataArea.getTopLeftX());
                areaToUpdate.setTopLeftY(dataArea.getTopLeftY());

                con.store(areaToUpdate);

                log.log(TAG, "Updated in DB: " + areaToUpdate.toString());
                return true;
            } else if (data instanceof PublicDisplay) {
                PublicDisplay dataDisplay = (PublicDisplay) data;
                PublicDisplay displayUpdate = read(dataDisplay).get(0); // TODO Empty/NullCheck
                displayUpdate.setCoordinateX(dataDisplay.getCoordinateX());
                displayUpdate.setCoordinateY(dataDisplay.getCoordinateY());
                displayUpdate.setIdentification(dataDisplay.getIdentification());
                displayUpdate.setLocation(dataDisplay.getLocation());
                displayUpdate.setToken(dataDisplay.getToken());
                con.store(displayUpdate);
                log.log(TAG, "Updated in DB: " + displayUpdate.toString());
                return true;
            } else if (data instanceof Location) {
                Location update = ((Location) data);
                Location original = ((Location) read(update).get(0)); // TODO Empty/NullCheck
                original.setWifiMorsels(update.getWifiMorsels());
                con.store(original);
                log.log(TAG, "Updated in DB: " + original.toString());
                return true;
            } else if (data instanceof WifiSensor) {
                WifiSensor update = ((WifiSensor) data);
                WifiSensor original = ((WifiSensor) read(update).get(0)); // TODO Empty/NullCheck
                original.setTokenHash(update.getTokenHash());
                original.setAccessDate(update.getAccessDate());
                con.store(original);
                log.log(TAG, "Updated in DB: "+original.toString());
                return true;
            }
            log.error(TAG, "Class update not implemented:" + data.toString());
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
    public boolean delete(Data data) {
        try {
            DataList<Data> dataList = read(data);
            // If the data isn't in the DB, the deletion wasn't required, but as the data isn't here, we return true.
            if (dataList != null && dataList.isEmpty()) {
                return false;
            } else if (dataList != null) {
                Data dataToDelete = dataList.get(0);
                con.delete(dataToDelete);
                log.log(TAG, dataToDelete.toString() + " deleted from DB!");
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes an object of type data from database.
     *
     * @param data the object to be deleted.
     * @return true if deletion was successful, otherwise false
     */
    public boolean deleteAll(Data data) {
        try {
            ObjectSet objects = con.query(data.getClass());
            while (objects.hasNext()) {
                con.delete(objects.next());
            }
            log.log(TAG, data.toString() + " deleted from DB!");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void init(String servletFilePath, boolean reinitialize) {
        Configuration config = Configuration.getInstance();
        config.init(servletFilePath); // must be first!!!
        log = Messenger.getInstance();
        String dbFilePath = servletFilePath + "WEB-INF/" + config.getDbName();

        EmbeddedConfiguration dbconfig = Db4oEmbedded.newConfiguration();
        dbconfig.common().objectClass(Area.class).cascadeOnUpdate(true);
        dbconfig.common().objectClass(Location.class).cascadeOnUpdate(true);
        //dbconfig.common().optimizeNativeQueries(true);
        //dbconfig.common().objectClass(User.class).objectField("email").indexed(true);
        //dbconfig.common().objectClass(Area.class).objectField("ID").indexed(true);
        con = Db4oEmbedded.openFile(dbconfig, dbFilePath);

        log.log(TAG, "db4o startup on " + dbFilePath);

        if (reinitialize)
            reinit();
    }

    /**
     * Must not be called before the
     */
    public void reinit() {
        //
        Configuration config = Configuration.getInstance();

        // Initializing Database
        log.log(TAG, "Running DB init.");
        DataList<Area> areaData = read(new Area("universe", null, 0, 0, 0, 0));
        if (areaData == null || areaData.isEmpty()) {
            log.log(TAG, "Universe not existing, creating it.");
            create(new Area("universe", new DataList<Location>(), 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        // Create default admin if no other admin exists
        User adminProto = new User(null);
        adminProto.setAdmin(true);
        DataList<User> adminData = read(adminProto);
        // test for existing single admin or list of admins
        if (adminData == null || adminData.isEmpty()) {
            log.log(TAG, "Admin not existing, creating one.");
            adminProto = new User(config.getAdminEmail(), config.getAdminName(), true);
            adminProto.setPwdHash(BCrypt.hashpw(config.getAdminPassword(), BCrypt.gensalt(12)));
            create(adminProto);
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
        if (con != null) {
            con.close();
            log.log(TAG, "db4o shutdown");
        }
    }
}
