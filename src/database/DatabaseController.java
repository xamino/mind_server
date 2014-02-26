package database;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import database.objects.DataList;
import database.objects.User;
import io.Configuration;
import logger.Messenger;

import java.util.List;

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

    /**
     * Deletes an object of type data from database.
     *
     * @param data the object to be deleted.
     * @return true if deletion was successful, otherwise false
     */
    public boolean delete(Data data) {
        try {
            Data dataToDelete = read(data);
            getConnection().delete(dataToDelete);
            log.log(CLASS, dataToDelete.toString() + " deleted from DB!");
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public Data readAll(final Data requestFilter) {
        ObjectContainer con = getConnection();

        List queryResult = null;

        if (requestFilter instanceof User) {
            final User user = (User) requestFilter;
            queryResult = con.query(new Predicate<User>() {
                @Override
                public boolean match(User o) {
                    return true;
                }
            });
        }

        DataList result = new DataList();
        for (Object o : queryResult) {
            result.add((Data) o);
        }

        //con.close();

        log.log(CLASS, result.toString() + " read from DB!");

        return result;
    }

    public Data read(final Data requestFilter) {
        ObjectContainer con = getConnection();

        List queryResult = null;

        if (requestFilter instanceof User) {
            final User user = (User) requestFilter;
            queryResult = con.query(new Predicate<User>() {
                @Override
                public boolean match(User o) {
                    // check on unique key
                    return o.getEmail().equals(user.getEmail());
                }
            });
        }

        Data result = null;
        if (queryResult.size() != 0) {
            result = (Data) queryResult.get(0);
        }
        //con.close();

        //log.log(CLASS, result.toString() + " read from DB!");

        return result;
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
