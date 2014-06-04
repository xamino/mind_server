package de.uulm.mi.mind.io;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.messages.Success;

import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cassio on 10.05.2014.
 */
public class DatabaseManager {
    private static DatabaseManager INSTANCE;
    private final String TAG = "DatabaseManager";
    private final DatabaseAccess dba;
    private final Messenger log;

    public DatabaseManager() {
        log = Messenger.getInstance();
        if (Configuration.getInstance().getDbType().toLowerCase().equals("sql")) {
            dba = DatabaseControllerSQL.getInstance();
        } else {
            dba = DatabaseController.getInstance();
        }
    }

    public static DatabaseManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseManager();
        }
        return INSTANCE;
    }

    public void init(ServletContextEvent servletContextEvent) {
        init(servletContextEvent, true);
    }

    public void destroy(ServletContextEvent servletContextEvent) {
        dba.destroy();
    }

    private void init(ServletContextEvent servletContextEvent, boolean reinitialize) {
        // Allow database to run initialization
        String filePath = servletContextEvent.getServletContext().getRealPath("/");
        dba.init(filePath);

        if (reinitialize)
            reinit();
    }

    void reinit() {
        open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // Initializing Database
                session.reinit();
                runMaintenance(session.getDb4oContainer()); // TODO
                return new Success("Reinitialized");
            }
        });
    }

    public Data open(Transaction transaction) {

        // get session object
        Session session = dba.open();

        // Give module access to do operations on a full transaction
        Data returnData = transaction.doOperations(session);

        // Evaluate Transaction Result
        if (returnData instanceof Error) {
            session.rollback();
        } else {
            session.commit();
        }

        // close session object
        session.close();

        return returnData;
    }

    private void runMaintenance(ObjectContainer rootContainer) {
        log.log(TAG, "In DB Stats:");
        List<User> dbUsers = rootContainer.query(new Predicate<User>() {
            @Override
            public boolean match(User o) {
                return true;
            }
        });
        log.log(TAG, "Users: " + dbUsers.size());
        List<Area> dbAreas = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return true;
            }
        });
        log.log(TAG, "Areas: " + dbAreas.size());
        List<Location> dbLocations = rootContainer.query(new Predicate<Location>() {
            @Override
            public boolean match(Location o) {
                return true;
            }
        });
        log.log(TAG, "Locations: " + dbLocations.size());
        List<WifiMorsel> dbMorsels = rootContainer.query(new Predicate<WifiMorsel>() {
            @Override
            public boolean match(WifiMorsel o) {
                return true;
            }
        });
        log.log(TAG, "Morsels: " + dbMorsels.size());

        /*

        // duplicate area:
        log.log(TAG, "Duplicate Check DB");
        checkDuplicates(new ArrayList<>(dbAreas));
        checkDuplicates(new ArrayList<>(dbLocations));
        ArrayList<WifiMorsel> duplicateMorsels = checkDuplicates(new ArrayList<>(dbMorsels));
        log.error(TAG, "Duplicate Morsels: " + duplicateMorsels.size());
        //checkDuplicates(dbAreas);
*/

        log.log(TAG, "In University Stats:");
        List<Area> set3 = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });
        if (!(set3 == null || set3.isEmpty())) {
            Area university = set3.get(0);
            log.log(TAG, "Locations: " + university.getLocations().size());
            ArrayList<WifiMorsel> wifs = new ArrayList<>();
            for (Location location : university.getLocations()) {
                wifs.addAll(location.getWifiMorsels());
            }
            log.log(TAG, "Morsels: " + wifs.size());

            /*
            //duplicates
            checkDuplicates(new ArrayList<>(university.getLocations()));
            duplicateMorsels = checkDuplicates(new ArrayList<>(wifs));
            log.error(TAG, "Duplicate Morsels: " + duplicateMorsels.size());
            */


        } else {
            log.log(TAG, "University does not exist!");
        }


        // clean university
        if (!(set3 == null || set3.isEmpty())) {
            Area university = set3.get(0);
            ArrayList<Location> duplicateLocations = checkDuplicates(new ArrayList<>(university.getLocations()));
            for (Location duplicateLocation : duplicateLocations) {
                List<Location> locs = read(new Location(duplicateLocation.getCoordinateX(), duplicateLocation.getCoordinateY()));
                if (locs.size() < 2) {
                    log.error(TAG, "Should be duplicate at least!");
                } else {
                    DataList<WifiMorsel> allmorsels = new DataList<>();
                    for (int i = 0; i < locs.size(); i++) {
                        allmorsels.addAll(locs.get(i).getWifiMorsels());
                        rootContainer.delete(locs.get(i));
                    }
                    duplicateLocation.setWifiMorsels(allmorsels);
                    rootContainer.store(duplicateLocation);
                }
            }

/*
            ArrayList<WifiMorsel> wifs = new ArrayList<>();
            for (Location location : university.getLocations()) {
                wifs.addAll(location.getWifiMorsels());
                duplicateMorsels = checkDuplicates(new ArrayList<>(location.getWifiMorsels()));
            }
            duplicateMorsels = checkDuplicates(new ArrayList<>(wifs));*/
        }
    }

    private <T extends Saveable> ArrayList<T> checkDuplicates(ArrayList<T> dbObjects) {
        ArrayList<T> duplicates = new ArrayList<>();
        ArrayList<T> dbObjects2 = new ArrayList<>(dbObjects);
        for (T dbObject : dbObjects) {
            ArrayList<T> duplicatesPlusOrig = new ArrayList<>();
            for (T object : dbObjects2) {
                if (dbObject instanceof WifiMorsel) {
                    WifiMorsel wifi1 = (WifiMorsel) dbObject;
                    WifiMorsel wifi2 = (WifiMorsel) object;
                    if (wifi1.getWifiMac().equals(wifi2.getWifiMac()) &&
                            wifi1.getDeviceModel().equals(wifi2.getDeviceModel()) &&
                            wifi1.getTimeStamp().equals(wifi2.getTimeStamp()) &&
                            wifi1.getWifiLevel() == wifi2.getWifiLevel() &&
                            wifi1.getWifiName().equals(wifi2.getWifiName()) &&
                            wifi1.getWifiChannel() == wifi2.getWifiChannel()) {
                        duplicatesPlusOrig.add(object);
                    }
                } else if (object.getKey().equals(dbObject.getKey()))
                    duplicatesPlusOrig.add(object);
            }
            if (duplicatesPlusOrig.size() > 1) {
                dbObjects2.removeAll(duplicatesPlusOrig);
                duplicates.add(dbObject);
                if (dbObject instanceof WifiMorsel) {
                    log.error(TAG, "Duplicate: " + duplicatesPlusOrig.size() + " " + dbObject);
                } else {
                    log.error(TAG, "Duplicate: " + duplicatesPlusOrig.size() + " " + dbObject.getKey());

                }
            }
        }
        return duplicates;
    }

    /**
     * Opens a new session on the database only for this very read operation.
     *
     * @param filter
     * @param <E>
     * @return
     */
    public <E extends Saveable> DataList<E> read(E filter) {
        Session session = dba.open();
        long time = System.currentTimeMillis();
        DataList<E> ret = session.read(filter);
        log.log(TAG, "sessionRead " + (System.currentTimeMillis() - time) + "ms");
        time = System.currentTimeMillis();
        session.close();
        log.log(TAG, "sessionClose " + (System.currentTimeMillis() - time) + "ms");
        return ret;
    }
}
