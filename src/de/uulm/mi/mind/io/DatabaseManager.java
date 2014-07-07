package de.uulm.mi.mind.io;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
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
                //runMaintenance(session.getDb4oContainer()); // TODO
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
        // read objects directly from DB. these are truly existing
        log.log(TAG, "In DB Stats:");
        log.log(TAG, "Users: " + rootContainer.query(User.class).size());
        log.log(TAG, "Areas: " + rootContainer.query(Area.class).size());
        log.log(TAG, "Locations: " + rootContainer.query(Location.class).size());
        log.log(TAG, "Morsels: " + rootContainer.query(WifiMorsel.class).size());


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
                if (location != null && location.getWifiMorsels()!=null)
                    wifs.addAll(location.getWifiMorsels());
            }
            log.log(TAG, "Morsels: " + wifs.size());
        } else {
            log.log(TAG, "University does not exist!");
        }


/*
        cleanDuplicates(rootContainer);
        rootContainer.commit();
        cleanNullLists(rootContainer);
        rootContainer.commit();
        cleanOrphans(rootContainer);
        rootContainer.commit();
        cleanNullLists(rootContainer);
        rootContainer.commit();
        */
    }

    private void cleanNullLists(ObjectContainer rootContainer) {
        List<Area> dbAreas = rootContainer.query(Area.class);
        for (Area dbArea : dbAreas) {
            while (dbArea.getLocations().remove(null)) ;
        }
        List<Location> dbLocations = rootContainer.query(Location.class);
        for (Location dbArea : dbLocations) {
            while (dbArea.getWifiMorsels().remove(null)) ;
        }
    }

    private void cleanDuplicates(ObjectContainer rootContainer) {
        List<Location> dbLocations = rootContainer.query(Location.class);
        ArrayList<Location> duplicateLocations = checkDuplicates(new ArrayList<>(dbLocations));
        int counter = 0;
        for (Location duplicateLocation : duplicateLocations) {
            List<Location> locs = rootContainer.queryByExample(new Location(duplicateLocation.getCoordinateX(), duplicateLocation.getCoordinateY()));
            if (locs.size() < 2) {
                log.error(TAG, "Location should be duplicate at least!");
            } else {
                DataList<WifiMorsel> allmorsels = locs.get(0).getWifiMorsels();
                for (int i = 1; i < locs.size(); i++) {
                    allmorsels.addAll(locs.get(i).getWifiMorsels());
                    rootContainer.delete(locs.get(i));
                    counter++;
                }
                locs.get(0).setWifiMorsels(allmorsels);
                rootContainer.store(locs.get(0));
            }
        }
        log.log(TAG, counter + " duplicate Locations removed");
        counter = 0;
        List<WifiMorsel> dbMorsels = rootContainer.query(WifiMorsel.class);
        ArrayList<WifiMorsel> duplicateMorsels = checkDuplicates(new ArrayList<>(dbMorsels));
        for (WifiMorsel duplicateMorsel : duplicateMorsels) {
            List<WifiMorsel> mors = rootContainer.queryByExample(duplicateMorsel);
            if (mors.size() < 2) {
                log.error(TAG, "Morsel should be duplicate at least!");
            } else {
                for (int i = 1; i < mors.size(); i++) {
                    rootContainer.delete(mors.get(i));
                    counter++;
                }
            }
        }
        log.log(TAG, counter + " duplicate Morsels removed");
    }

    private void cleanOrphans(ObjectContainer rootContainer) {
        ObjectSet<Location> dbLocations = rootContainer.query(Location.class);
        ObjectSet<WifiMorsel> dbMorsels = rootContainer.query(WifiMorsel.class);
        ObjectSet<Area> set3 = rootContainer.query(new Predicate<Area>() {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });
        Area university = set3.get(0);
        int counter = 0;
        for (Location location : dbLocations) {
            if (!university.getLocations().contains(location)) {
                rootContainer.delete(location);
                counter++;
            }
        }
        log.log(TAG, "Orphaned Locations removed: " + counter);

        int mCounter = 0;
        int c = 0;
        for (WifiMorsel dbMorsel : dbMorsels) {
            //log.log(TAG, ++c + ": " + dbMorsel.toString());
            boolean isOrphan = true;
            for (Location location : university.getLocations()) {
                for (WifiMorsel morsel : location.getWifiMorsels()) {
                    if (morsel.getWifiMac().equals(dbMorsel.getWifiMac())
                            && (morsel.getWifiLevel() == dbMorsel.getWifiLevel())
                            && (morsel.getWifiChannel() == dbMorsel.getWifiChannel())
                            && (morsel.getDeviceModel().equals(dbMorsel.getDeviceModel()))
                            && (morsel.getWifiName().equals(dbMorsel.getWifiName()))
                            && (morsel.getTimeStamp().equals(dbMorsel.getTimeStamp()))) {
                        isOrphan = false;
                    }
                }
            }
            if (isOrphan) {
                rootContainer.delete(dbMorsel);
                mCounter++;
            }
        }

        log.log(TAG, "Orphaned Morsels removed: " + mCounter);

    }

    private <T extends Saveable> ArrayList<T> checkDuplicates(ArrayList<T> dbObjects) {
        ArrayList<T> duplicates = new ArrayList<>();
        ArrayList<T> dbObjects2 = new ArrayList<>(dbObjects);
        for (T dbObject : dbObjects) {
            int count = 0;
            for (int i = dbObjects2.size() - 1; i >= 0; i--) {
                if (dbObject instanceof WifiMorsel) {
                    WifiMorsel wifi1 = (WifiMorsel) dbObject;
                    WifiMorsel wifi2 = (WifiMorsel) dbObjects2.get(i);
                    if (wifi1.getWifiMac().equals(wifi2.getWifiMac()) &&
                            wifi1.getDeviceModel().equals(wifi2.getDeviceModel()) &&
                            wifi1.getTimeStamp().equals(wifi2.getTimeStamp()) &&
                            wifi1.getWifiLevel() == wifi2.getWifiLevel() &&
                            wifi1.getWifiName().equals(wifi2.getWifiName()) &&
                            wifi1.getWifiChannel() == wifi2.getWifiChannel()) {
                        dbObjects2.remove(i);
                        count++;
                    }
                } else if (dbObjects2.get(i).getKey().equals(dbObject.getKey())) {
                    dbObjects2.remove(i);
                    count++;
                }
            }
            if (count > 1) {
                duplicates.add(dbObject);
                if (dbObject instanceof WifiMorsel) {
                    //log.error(TAG, "Duplicate: " + count + " " + dbObject);
                } else {
                    //log.error(TAG, "Duplicate: " + count + " " + dbObject.getKey());

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
        return read(filter, 1);
    }

    /**
     * Opens a new session on the database only for this very read operation.
     *
     * @param filter
     * @param depth  Depths of children to be returned.
     *               0 = object fields are initialized to defaults,
     *               1 object fields loaded, 2 children are initialized to defaults,
     *               3 children field loaded etc;
     *               e.g. 5 could be Area-DataList-Location-DataList-WifiMorsel loaded but no further DataList
     * @param <E>
     * @return
     */
    public <E extends Saveable> DataList<E> read(E filter, int depth) {
        Session session = dba.openRoot();
        // long time = System.currentTimeMillis();
        DataList<E> ret = session.read(filter, depth);
        // log.log(TAG, "sessionRead " + (System.currentTimeMillis() - time) + "ms " + filter);
        // time = System.currentTimeMillis();

        DataList<E> cloned = new DataList<>();
        for (E e : ret) {
            cloned.add((E) e.deepClone());
        }

        // log.log(TAG, "sessionClose " + (System.currentTimeMillis() - time) + "ms");
        return cloned;
    }
}
