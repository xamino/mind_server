/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import database.Data;
import database.objects.*;
import database.objects.Error;
import io.Configuration;
import logger.Messenger;
import logic.EventModuleManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Dieses Servlet ist fuer alle oeffentlich zugaengliche Daten zustaendig.
 * Insbesondere betrifft dies den Abruf aller verfuegbaren Angebote aus der
 * Datenbank fuer den Index.
 */

@WebServlet("/Servlet/*")
public class Servlet extends HttpServlet {

    /**
     * TAG for logging.
     */
    private final String TAG = "Servlet";
    /**
     * JSON library.
     */
    private Gson gson;
    /**
     * Class for logging stuff.
     */
    private Messenger log;
    private Sanitation sanitation;
    private EventModuleManager moduleManager;

    @Override
    public void init() throws ServletException {
        super.init();
        Configuration.getInstance().init(getServletContext()); // must be first!!!

        //JSON stuff
        // Base data type that we use for JSON data: ($type is the attribute name)
        RuntimeTypeAdapterFactory<Data> factory = RuntimeTypeAdapterFactory.of(Data.class, "$type");
        // Register all JSON-objects: (The strings are the types, must be unique!)
        factory.registerSubtype(Location.class, "Location");
        factory.registerSubtype(Message.class, "Message");
        factory.registerSubtype(Error.class, "Error");
        factory.registerSubtype(Success.class, "Success");
        factory.registerSubtype(WifiMorsel.class, "WifiMorsel");
        factory.registerSubtype(User.class, "User");
        factory.registerSubtype(Arrival.class, "Arrival");
        // Register adapter
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(factory);
        gson = builder.create();

        log = Messenger.getInstance();
        sanitation = Sanitation.getInstance();
        moduleManager = EventModuleManager.getInstance();
    }

    /**
     * This method receives all json requests that will result in a write or modify of objects.
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // Get arrival object
        // Watch out, arrival.getData() might be NULL!
        Arrival arrival = getRequest(request);
        // Write whatever you want sent back to this object:
        Data answer = null;
        // Check if valid:
        answer = sanitation.checkArrival(arrival);
        // If answer is still null, everything checks out (otherwise an error or message object would be in place
        // already)
        if (answer == null) {
            // This means valid session and arrival!
            String task = arrival.getTask();
            switch (task) {
                case "test":
                    ArrayList<WifiMorsel> wifis = new ArrayList<>();
                    wifis.add(new WifiMorsel("0:0","WIFIS",-86));
                    wifis.add(new WifiMorsel("A3:34","EDUROAM",-03));
                    Location loc = new Location(4.0, 2.0, wifis);
                    answer = loc;
                    break;
                case "logout":
                    sanitation.destroySession(arrival.getSessionHash());
                    answer = new Message("Logged out!");
                    break;
                case "echo":
                    // Simple echo test for checking if the server can parse the data
                    answer = arrival.getObject();
                    break;
                default:
                    log.log(TAG, "Unknown task sent: " + task);
                    answer = new Error("POST illegal TASK", "Unknown task: <" + task + ">");
                    break;
            }
        }
        prepareDeparture(response, answer);
    }

    /**
     * This method handles all simple data requests. ONLY USE FOR SIMPLE DEBUGGING, AS IT IS NOT SECURED!
     *
     * @param request
     * @param response
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        // Use only for DEBUGGING AND TESTS!
        // All production code should go in POST
        User a = new User("Hans", "hans@peter.de");
        moduleManager.handleTask(Task.UserTask.CREATE_USER, a);

        User t = (User) moduleManager.handleTask(Task.UserTask.READ_USER, a);

        t.setName("Gustav");

        moduleManager.handleTask(Task.UserTask.UPDATE_USER, t);

        User b = new User("Tom", "tom@jerry.de");
        User c = new User("Jerry", "jerry@tom.de");
        moduleManager.handleTask(Task.UserTask.CREATE_USER, b);
        moduleManager.handleTask(Task.UserTask.CREATE_USER, c);


       Data userList = moduleManager.handleTask(Task.UserTask.READ_USERS,null);
       log.log("Servlet", userList.toString());

        moduleManager.handleTask(Task.UserTask.DELETE_USER, a);
        moduleManager.handleTask(Task.UserTask.DELETE_USER, b);
        moduleManager.handleTask(Task.UserTask.DELETE_USER, c);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * Creates JSON object out of answer and encapsulates it in the type object.
     *
     * @param response The response where the data will be written.
     * @param answer   The object to attach.
     * @throws IOException
     */
    // TODO $type is still not being written... maybe do manually? But should be done, ??
    private void prepareDeparture(HttpServletResponse response, Data answer) throws IOException {
        // Must be done before write:
        response.setCharacterEncoding("UTF-8");
        // If this happens, send back a standard error message.
        if (answer == null) {
            answer = new Error("Empty ANSWER", "Answer does not contain an object! Make sure your request is valid!");
        }
        response.getWriter().write(gson.toJson(answer, answer.getClass()));
        response.setContentType("application/json");
    }

    /**
     * Takes the HttpServletRequest and returns the correct arrival object filled with JSON goodies.
     *
     * @param request The request to read.
     * @return The Arrival object filled with the data.
     * @throws IOException
     */
    private Arrival getRequest(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        String out = "";
        do {
            String value = reader.readLine();
            if (value == null || value.isEmpty())
                break;
            out += value;
        } while (true);
        // Better safe than sorry:
        if (out.isEmpty())
            return null;
        // parse the object out:
        return gson.fromJson(out, Arrival.class);
    }
}
