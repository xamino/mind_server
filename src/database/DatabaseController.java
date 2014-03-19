package database;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import database.objects.Area;
import database.objects.DataList;
import database.objects.Location;
import database.objects.User;
import io.Configuration;
import logger.Messenger;
import servlet.BCrypt;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * @author Tamino Hartmann
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
        // avoid duplicates
        DataList<Data> readData = read(data);
        if (readData != null && !readData.isEmpty()) {
            return false;
        }

        try {
            con.store(data);
            //con.close();
            log.log(TAG, data.toString() + " written to DB!");

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
        List queryResult = null;

        try {
            // Differentiate Object types
            if (requestFilter instanceof User) {
                final User user = (User) requestFilter;

                // unique key of the User object is the email field.
                // When this is empty directly use the user Object as filter.
                if (user.getEmail() == null) {
                    queryResult = con.queryByExample(user);
                } else { // When the email key is set, only return the single instance matching the key
                    queryResult = con.query(new Predicate<User>() {
                        @Override
                        public boolean match(User o) {
                            // email is Users unique key
                            return o.getEmail().equals(user.getEmail());
                        }
                    });

                }

            } else if (requestFilter instanceof Area) {
                final Area area = (Area) requestFilter;

                // unique key of the Area object is the ID field.
                // When this is empty directly use the user Object as filter.
                if (area.getID() == null && (area.getLocations() == null || area.getLocations().isEmpty())) {
                    queryResult = con.queryByExample(area);
                } else {
                    queryResult = con.query(new Predicate<Area>() {
                        @Override
                        public boolean match(Area match) {
                            // return all areas that include the single location contained in the requestfilter
                            if (area.getID() == null && area.getLocations() != null && area.getLocations().size() == 1) {
                                Location contained = area.getLocations().get(0);
                                double x = contained.getCoordinateX();
                                double y = contained.getCoordinateY();
                                return (x >= match.getTopLeftX())
                                        && (y >= match.getTopLeftY())
                                        && (x <= (match.getTopLeftX() + match.getWidth()))
                                        && (y <= (match.getTopLeftY() + match.getHeight()));
                            } else /// ID is Areas unique key
                                return match.getID().equals(area.getID());
                        }
                    });
                }
            } else if (requestFilter instanceof Location) {
                final Location loc = (Location) requestFilter;
                if (loc.getCoordinateX() == 0 && loc.getCoordinateY() == 0) {
                    queryResult = con.queryByExample(loc);
                } else {
                    queryResult = con.query(new Predicate<Location>() {
                        @Override
                        public boolean match(Location o) {
                            // check on unique key
                            return loc.getCoordinateY() == o.getCoordinateY() && loc.getCoordinateX() == o.getCoordinateX();
                        }
                    });
                }
            }

            // Write query results to DataList
            DataList<T> result = new DataList<>();
            if (queryResult != null && queryResult.size() > 0) {
                for (Object o : queryResult) {
                    result.add((T) o);
                }
            }
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

            log.log(TAG, result.toString() + " read from DB!");
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean update(Data data) {
        try {
            if (data instanceof User) {
                User dataUser = (User) data;
                User userToUpdate = read(dataUser).get(0); // TODO Empty/NullCheck
                userToUpdate.setName(dataUser.getName());
                userToUpdate.setPwdHash(dataUser.getPwdHash());
                userToUpdate.setAdmin(dataUser.isAdmin());

                con.store(userToUpdate);

                log.log(TAG, userToUpdate.toString() + " updated in DB!");

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

                log.log(TAG, areaToUpdate.toString() + " updated in DB!");
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
     * @return true if deletion was successful, otherwise false
     */
    public boolean delete(Data data) {
        try {
            Data dataToDelete = read(data);
            // TODO @Andy: passt das so? READ gibt hier eine liste der length 1 zurück!
            if (dataToDelete instanceof DataList) {
                dataToDelete = (Data) ((DataList) dataToDelete).get(0);
            }
            con.delete(dataToDelete);
            log.log(TAG, dataToDelete.toString() + " deleted from DB!");
            return true;
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

    public void init() {
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
        User adminProto = new User(null, null);
        adminProto.setAdmin(true);
        DataList<User> adminData = read(adminProto);
        // test for existing single admin or list of admins
        if (adminData == null || adminData.isEmpty()) {
            log.log(TAG, "Admin not existing, creating one.");
            adminProto = new User(config.getAdminName(), config.getAdminEmail());
            adminProto.setAdmin(true);
            adminProto.setPwdHash(BCrypt.hashpw(config.getAdminPassword(), BCrypt.gensalt(12)));
            create(adminProto);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        Configuration config = Configuration.getInstance();
        config.init(context); // must be first!!!
        log = Messenger.getInstance();

        String filePath = context.getRealPath("WEB-INF/"
                + config.getDbName());
        con = Db4oEmbedded.openFile(filePath);

        context.log("db4o startup on " + filePath);

        init();
    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        close(con);
        context.log("db4o shutdown");
    }

    private void close(ObjectContainer container) {
        if (container != null) {
            container.close();
        }
    }
}
