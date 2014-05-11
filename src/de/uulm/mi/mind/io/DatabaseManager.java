package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Cassio on 10.05.2014.
 */
public class DatabaseManager implements ServletContextListener {
    private static DatabaseManager INSTANCE;
    private final DatabaseAccess dba;
    //private final DatabaseAccess mySQL;

    private DatabaseManager() {
        dba = DatabaseController.getInstance();
        //mySQL = DatabaseControllerSQL.getInstance();
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

    public static DatabaseManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseManager();
        }
        return INSTANCE;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        dba.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        dba.destroy();
    }
}
