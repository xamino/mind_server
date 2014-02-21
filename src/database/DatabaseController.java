package database;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
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
        return Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), "BLUBBASE");
    }

    public boolean delete(Data data) {
        return false;
    }

    public boolean update(Data data) {
        return false;
    }

    public Data read(final Data request) {
        ObjectContainer con = getConnection();

        List result = null;

        if(request instanceof User){
            final User user = (User)request;
            result = con.query(new Predicate<User>() {
                @Override
                public boolean match(User o) {
                    return o.getEmail().equals(user.getEmail());
                }
            });
        }

        Data singleResult = (result == null || result.size() == 0) ? null : (Data)result.get(0);

        con.close();

        return singleResult;
    }

    public boolean create(Data data) {
        ObjectContainer con = getConnection();
        con.store(new User("Blub1", "blub1@mail.de"));
        con.store(data);
        con.store(new User("Blub", "blub@mail.de"));
        con.close();

        log.log(CLASS, "done writing!");

        return true;
    }


    public static void listResult(List<?> result){
        System.out.println(result.size());
        for (Object o : result) {
            System.out.println(o);
        }
    }
}
