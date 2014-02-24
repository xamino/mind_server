package database;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.ext.Db4oDatabase;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import database.objects.Area;
import database.objects.Location;
import database.objects.User;
import io.Configuration;
import logger.Messenger;

import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

/**
 * @author Tamino Hartmann
 */
public class DatabaseController {
    /**
     * Variable for storing the instance of the class.
     */
    private static DatabaseController instance;
    private final Messenger log;
    private final String CLASS = "DatabaseController";
    private String url;
    private ObjectContainer connection;

    /**
     * Verbingung zur Datenbank
     */

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
     * Private constructor for DatabaseController for implementing the singleton
     * instance. Use getInstance() to get a reference to an object of this type.
     */
    private DatabaseController() {
        log = Messenger.getInstance();
        Configuration config = Configuration.getInstance();
    }

    private ObjectContainer getConnection() {
        if (connection == null) {
            connection = Db4oEmbedded.openFile(Db4oEmbedded
                    .newConfiguration(), "MIND_DB");
        }
        return connection;
    }

    public boolean delete(Data data) {

        if (data instanceof User) {
            User dataUser = (User) data;
            User userToDelete = (User) read(data);

            getConnection().delete(userToDelete);

            log.log(CLASS, userToDelete.toString() + " deleted from DB!");

            return true;
        } else return false;
    }

    public boolean update(Data data) {

        if (data instanceof User) {
            User dataUser = (User) data;
            User userToUpdate = (User) read(data);
            userToUpdate.setName(dataUser.getName());
            userToUpdate.setPwdHash(dataUser.getPwdHash());

            getConnection().store(userToUpdate);

            log.log(CLASS, userToUpdate.toString() + " updated in DB!");

            return true;
        } else return false;

    }

    public Data read(final Data requestFilter) {
        ObjectContainer con = getConnection();

        List result = null;

        if (requestFilter instanceof User) {
            final User user = (User) requestFilter;
            result = con.query(new Predicate<User>() {
                @Override
                public boolean match(User o) {
                    return o.getEmail().equals(user.getEmail());
                }
            });
        }

        Data singleResult = (result == null || result.size() == 0) ? null : (Data) result.get(0);

        //con.close();

        log.log(CLASS, singleResult.toString() + " read from DB!");

        return singleResult;
    }

    public boolean create(Data data) {
        ObjectContainer con = getConnection();
        con.store(data);
        //con.close();

        log.log(CLASS, data.toString() + " written to DB!");

        return true;
    }


    public static void listResult(List<?> result) {
        System.out.println(result.size());
        for (Object o : result) {
            System.out.println(o);
        }
    }
}
