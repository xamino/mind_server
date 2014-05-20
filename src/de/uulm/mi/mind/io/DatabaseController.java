package de.uulm.mi.mind.io;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.security.BCrypt;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.List;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
class DatabaseController implements DatabaseAccess {
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
    public boolean create(Session session, Saveable data) {
        ObjectContainer sessionContainer = session.getDb4oContainer();
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
     * @param <T>           A DataType extending Data.
     * @return A DataList of the specified filter parameter Type or null on error.
     */
    @Override
    public <T extends Saveable> DataList<T> read(Session session, final T requestFilter) {
        ObjectContainer sessionContainer = session.getDb4oContainer();
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

    @Override
    public boolean update(Session session, Saveable data) {
        ObjectContainer sessionContainer = session.getDb4oContainer();
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
    @Override
    public boolean delete(Session session, Saveable data) {
        ObjectContainer sessionContainer = session.getDb4oContainer();
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

    private ObjectContainer getSessionContainer() {
        // open the db4o-session.
        return rootContainer.ext().openSession();
    }

    public void init(String servletFilePath, boolean reinitialize) {
        Configuration config = Configuration.getInstance();
        log = Messenger.getInstance();
        String dbFilePath = servletFilePath + "WEB-INF/" + config.getDbName();

        EmbeddedConfiguration dbconfig = Db4oEmbedded.newConfiguration();
        //dbconfig.common().diagnostic().addListener(new DiagnosticToConsole());
        dbconfig.common().objectClass(Area.class).cascadeOnUpdate(true);
        dbconfig.common().objectClass(Location.class).cascadeOnUpdate(true);
        dbconfig.common().objectClass(Location.class).cascadeOnDelete(true);
        //dbconfig.common().optimizeNativeQueries(true);
        dbconfig.common().objectClass(User.class).objectField("email").indexed(true);
        dbconfig.common().objectClass(Area.class).objectField("ID").indexed(true);
        dbconfig.common().objectClass(PublicDisplay.class).objectField("identification").indexed(true);
        dbconfig.common().objectClass(WifiSensor.class).objectField("identification").indexed(true);
        dbconfig.common().objectClass(Location.class).objectField("key").indexed(true);
        rootContainer = Db4oEmbedded.openFile(dbconfig, dbFilePath);

        log.log(TAG, "db4o startup on " + dbFilePath);

        runMaintenance(rootContainer);

        if (reinitialize) {
            reinit(new Session(rootContainer, getInstance()));
            rootContainer.commit();
        }

    }

    private void runMaintenance(ObjectContainer rootContainer) {
        log.log(TAG, "In DB Stats:");

        ObjectSet<Area> set2 = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return true;
            }
        });
        log.log(TAG, "Areas: " + set2.size());
        ObjectSet<Location> set1 = rootContainer.query(new Predicate<Location>() {
            @Override
            public boolean match(Location o) {
                return true;
            }
        });
        log.log(TAG, "Locations: " + set1.size());
        ObjectSet<WifiMorsel> set = rootContainer.query(new Predicate<WifiMorsel>() {
            @Override
            public boolean match(WifiMorsel o) {
                return true;
            }
        });
        log.log(TAG, "Morsels: " + set.size());

        log.log(TAG, "In Universe:");
        ObjectSet<Area> set3 = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });

        if (set3.size() == 0) {
            log.log(TAG, "Nothing, does not yet exist!");
            return;
        }
        Area university = set3.get(0);
        log.log(TAG, "Locations: " + university.getLocations().size());
        int morselCounter = 0;
        for (Location location : university.getLocations()) {
            morselCounter += location.getWifiMorsels().size();
        }
        log.log(TAG, "Morsels: " + morselCounter);


        //cleanOrphans(rootContainer);

        /*for (WifiMorsel wifiMorsel : set) {
            if (wifiMorsel.getWifiName().equals("eduroam") || wifiMorsel.getWifiName().equals("welcome"))
                rootContainer.delete(wifiMorsel);
        }
*/
        /*ObjectSet<Location> locs = rootContainer.query(Location.class);
        for (Location loc : locs){
            loc.setCoordinateX(loc.getCoordinateX());
            rootContainer.store(loc);
        }*/
    }

    private void cleanOrphans(ObjectContainer rootContainer) {
        ObjectSet<Location> set1 = rootContainer.query(new Predicate<Location>() {
            @Override
            public boolean match(Location o) {
                return true;
            }
        });
        ObjectSet<WifiMorsel> allDBMorsels = rootContainer.query(new Predicate<WifiMorsel>() {
            @Override
            public boolean match(WifiMorsel o) {
                return true;
            }
        });
        ObjectSet<Area> set3 = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });
        Area university = set3.get(0);

        int counter = 0;
        for (Location location : set1) {
            if (!university.getLocations().contains(location)) {
                rootContainer.delete(location);
                counter++;
            }
        }
        log.log(TAG, "Orphaned Locations removed: " + counter);

        int mCounter = 0;
        int c = 0;
        for (WifiMorsel dbMorsel : allDBMorsels) {
            log.log(TAG, ++c + ": " + dbMorsel.toString());
            boolean isOrphan = true;
            for (Location location : university.getLocations()) {
                for (WifiMorsel morsel : location.getWifiMorsels()) {
                    if (morsel.getWifiMac().equals(dbMorsel.getWifiMac())
                            && (morsel.getWifiLevel() == dbMorsel.getWifiLevel())
                            && (morsel.getWifiChannel() == dbMorsel.getWifiChannel())
                            && bothNullOrEqual(morsel.getDeviceModel(), dbMorsel.getDeviceModel())
                            && (morsel.getWifiName().equals(dbMorsel.getWifiName()))) {
                        isOrphan = false;
                        //break;
                    }
                }
                //if (!isOrphan) break;
            }
            if (isOrphan) {
                rootContainer.delete(dbMorsel);
                mCounter++;
            }
        }

        log.log(TAG, "Orphaned Morsels removed: " + mCounter);

    }

    private boolean bothNullOrEqual(String deviceModel, String deviceModel1) {
        if (deviceModel == null && deviceModel1 == null) return true;
        else if (deviceModel != null && deviceModel1 != null) {
            return deviceModel.equals(deviceModel1);
        } else return false;
    }

    /**
     * Must not be called before the
     */
    public void reinit(Session session) {
        ObjectContainer sessionContainer = session.getDb4oContainer();
        //
        Configuration config = Configuration.getInstance();

        // Initializing Database
        log.log(TAG, "Running DB init.");
        DataList<Area> areaData = read(session, new Area("University"));
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

    public void close() {

    }

    @Override
    public Session open() {
        return new Session(getSessionContainer(), getInstance());
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
