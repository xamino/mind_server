package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.messages.Success;

import javax.servlet.ServletContextEvent;
import java.util.List;

/**
 * Created by Cassio on 10.05.2014.
 */
public class DatabaseManager {
    private static DatabaseManager INSTANCE;
    private final String TAG = "DatabaseManager";
    private final DatabaseAccess dba;
    private final Messenger log;
    //private final DatabaseAccess mySQL;

    public DatabaseManager() {
        log = Messenger.getInstance();
        dba = DatabaseController.getInstance();
        //dba = DatabaseControllerSQL.getInstance();
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
        dba.destroy(servletContextEvent);
    }

    private void init(ServletContextEvent servletContextEvent, boolean reinitialize) {
        // Allow database to run initialization
        dba.init(servletContextEvent);


        if (reinitialize)
            reinit();
    }

    private void reinit() {
        open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // Initializing Database
                session.reinit();
                //runMaintenance(session.getSqlContainer()); // TODO
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

    private void runMaintenance(ObjectContainerSQL rootContainer) {
        log.log(TAG, "In DB Stats:");
        List<User> set4 = rootContainer.query(new Predicate<User>(User.class) {
            @Override
            public boolean match(User o) {
                return true;
            }
        });
        log.log(TAG, "Users: " + set4.size() + "\n" + set4);
        List<Area> set2 = rootContainer.query(new Predicate<Area>(Area.class) {
            @Override
            public boolean match(Area o) {
                return true;
            }
        });
        log.log(TAG, "Areas: " + set2.size() + "\n" + set2);
        List<Location> set1 = rootContainer.query(new Predicate<Location>(Location.class) {
            @Override
            public boolean match(Location o) {
                return true;
            }
        });
        log.log(TAG, "Locations: " + set1.size() + "\n" + set1);
        List<WifiMorsel> set = rootContainer.query(new Predicate<WifiMorsel>(WifiMorsel.class) {
            @Override
            public boolean match(WifiMorsel o) {
                return true;
            }
        });
        log.log(TAG, "Morsels: " + set.size() + "\n" + set);

        /*
        log.log(TAG, "In Universe:");
        List<Area> set3 = rootContainer.query(new Predicate<Area>(Area.class) {
            @Override
            public boolean match(Area o) {
                return o.getKey().equals("University");
            }
        });
        if (set3 == null || set3.size() == 0) {
            return;
        }
        Area university = set3.get(0);
        log.log(TAG, "Locations: " + university.getLocations().size());
        int morselCounter = 0;
        for (Location location : university.getLocations()) {
            morselCounter += location.getWifiMorsels().size();
        }
        log.log(TAG, "Morsels: " + morselCounter);
        */
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
        DataList<E> ret = session.read(filter);
        session.close();
        return ret;
    }
}
