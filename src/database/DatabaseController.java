package database;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.constraints.UniqueFieldValueConstraint;
import com.db4o.query.Predicate;
import database.objects.*;
import io.Configuration;
import logger.Messenger;
import logic.Task;

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
    private final String dbName;
    private ObjectContainer connection;

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
        dbName = config.getDbName();
    }

    /**
     * Returns an instance of the Database.
     *
     * @return A Database Connection
     */
    private ObjectContainer getConnection() {
        if (connection == null) {
            EmbeddedConfiguration conf = Db4oEmbedded.newConfiguration();
            connection = Db4oEmbedded.openFile(conf, dbName); // TODO configuration (user, password, location)
        }
        return connection;
    }

    /**
     * Store a Data object in the Database.
     *
     * @param data The Object to be stored.
     * @return true if the operation was successful and false otherwise.
     */
    public boolean create(Data data) {
        // avoid duplicates
        if(read(data) != null){
            return false;
        }

        try {
            ObjectContainer con = getConnection();
            con.store(data);

            log.log(CLASS, data.toString() + " written to DB!");

            return true;
        } catch (Exception e) {
            return false;
        }
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
        if (queryResult != null && queryResult.size() > 0) {
            result = (Data) queryResult.get(0);
        }

        if (result != null) {
            log.log(CLASS, result.toString() + " read from DB!");
        }

        return result;
    }

    public Data readAll(final Data requestFilter) {
        try {
            ObjectContainer con = getConnection();

            List queryResult = con.query(requestFilter.getClass());

            // write query result into a Datalist
            DataList result = new DataList();
            if (queryResult != null) {
                for (Object o : queryResult) {
                    result.add((Data) o);
                }
            }

            log.log(CLASS, result.toString() + " read from DB!");

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reads child lists of the specified Data object if any available.
     *
     * @param requestFilter The Data object of which the children should be fetched.
     * @return a Datalist of children or null on error.
     */
    public Data readChildren(Data requestFilter) {
        try {
            ObjectContainer con = getConnection();

            List queryResult = null;
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
                    result = ((Location) queryResult.get(0)).getWifiNetworks();
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
                //TODO
            }
            if (result == null)
                result = new DataList();

            log.log(CLASS, result.toString() + " read from DB!");
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean update(Data data) {
        try {
            if (data instanceof User) {
                User dataUser = (User) data;
                User userToUpdate = (User) read(data);
                userToUpdate.setName(dataUser.getName());
                userToUpdate.setPwdHash(dataUser.getPwdHash());

                getConnection().store(userToUpdate);

                log.log(CLASS, userToUpdate.toString() + " updated in DB!");

                return true;
            } else return false;
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

    public static void listResult(List<?> result) {
        System.out.println(result.size());
        for (Object o : result) {
            System.out.println(o);
        }
    }
}
