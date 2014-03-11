package database;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
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
    private  Messenger log;
    private final String CLASS = "DatabaseController";
    private ObjectContainer con;

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
    public DatabaseController() {
    }

    /**
     * Store a Data object in the Database.
     *
     * @param data The Object to be stored.
     * @return true if the operation was successful and false otherwise.
     */
    public boolean create(Data data) {
        // avoid duplicates
        if (read(data) != null) {
            return false;
        }

        try {
            con.store(data);
            //con.close();
            log.log(CLASS, data.toString() + " written to DB!");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public <T extends Data> T read(final T requestFilter) {
        List queryResult = null;

        // Read Data depending on the objects unique key
        if (requestFilter instanceof User) {
            final User user = (User) requestFilter;
            queryResult = con.query(new Predicate<User>() {
                @Override
                public boolean match(User o) {
                    // check on unique key
                    return o.getEmail().equals(user.getEmail());
                }
            });
        } else if (requestFilter instanceof Area) {
            final Area area = (Area) requestFilter;
            queryResult = con.query(new Predicate<Area>() {
                @Override
                public boolean match(Area o) {
                    // check on unique key
                    return o.getID().equals(area.getID());
                }
            });
        } else if (requestFilter instanceof Location) {
            final Location loc = (Location) requestFilter;
            queryResult = con.query(new Predicate<Location>() {
                @Override
                public boolean match(Location o) {
                    // check on unique key
                    return loc.getCoordinateY() == o.getCoordinateY() && loc.getCoordinateX() == o.getCoordinateX();
                }
            });
        }

        T result = null;
        if (queryResult != null && queryResult.size() > 0) {
            result = (T) queryResult.get(0); // TODO unchecked!
        }

        /*if (result != null) {
            log.log(CLASS, result.toString() + " read from DB!");
        }*/

        return result;
    }

    public <T extends Data> DataList<T> readAll(final T requestFilter) {
        try {
            List queryResult = con.query(requestFilter.getClass());

            // write query result into a Datalist
            DataList<T> result = new DataList<>();
            if (queryResult != null) {
                for (Object o : queryResult) {
                    result.add((T) o);
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

                con.store(userToUpdate);

                log.log(CLASS, userToUpdate.toString() + " updated in DB!");

                return true;
            } else if (data instanceof Area) {
                Area dataArea = (Area) data;
                Area areaToUpdate = (Area) read(data);
                areaToUpdate.setID(dataArea.getID());
                areaToUpdate.setLocations(dataArea.getLocations());

                con.store(areaToUpdate);

                log.log(CLASS, areaToUpdate.toString() + " updated in DB!");
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
            con.delete(dataToDelete);
            log.log(CLASS, dataToDelete.toString() + " deleted from DB!");
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
            con.delete(data);
            log.log(CLASS, data.toString() + " deleted from DB!");
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

    public void init() {
        // Initializing Database
        if (read(new Area("universe", null, 0, 0, 0, 0)) == null) {
            create(new Area("universe", new DataList<Location>(), 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        User adminProto = new User("", "");
        adminProto.setAdmin(true);
        if (readAll(adminProto).isEmpty()) {
            adminProto = new User("admin", "admin@admin.admin");
            adminProto.setAdmin(true);
            adminProto.setPwdHash(BCrypt.hashpw("admin", BCrypt.gensalt(12)));
            create(adminProto);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log = Messenger.getInstance();
        Configuration config = Configuration.getInstance();
        String dbName = config.getDbName();

        ServletContext context = event.getServletContext();
        String filePath = context.getRealPath("WEB-INF/"
                + dbName);
        EmbeddedObjectContainer rootContainer = Db4oEmbedded.openFile(filePath);
        con = rootContainer;
        //context.setAttribute("DB_SERVER", rootContainer);
        context.log("db4o startup on " + filePath);

        init();
    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        //ObjectContainer rootContainer = (ObjectContainer) context.getAttribute("DB_SERVER");
        //context.removeAttribute("DB_SERVER");
        close(con);
        context.log("db4o shutdown");
    }

    private void close(ObjectContainer container) {
        if (container != null) {
            container.close();
        }
        container = null;
    }
}
