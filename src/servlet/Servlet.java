/**
 * @author Tamino Hartmann
 */

package servlet;

import database.Data;
import database.Information;
import database.messages.Error;
import database.objects.Area;
import database.objects.DataList;
import database.objects.Location;
import database.objects.User;
import io.Configuration;
import logger.Messenger;
import logic.EventModuleManager;
import logic.Task;
import logic.modules.SanitationModule;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

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
    private JsonConverter json;
    /**
     * Class for logging stuff.
     */
    private Messenger log;
    private SanitationModule sanitationModule;
    private EventModuleManager moduleManager;

    /**
     * Method that checkes if data returned from the modules is a message, in which case it is returned, or null, in
     * which case an error message is returned. If the data is anything else, it is considered to be a valid reply and
     * null is returned.
     *
     * @param data The data to check.
     * @return An Information object if data is such, else null.
     */
    // TODO public static is ugly, do something!
    public static Information checkDataMessage(Data data) {
        if (data == null) {
            return new Error("DATA NULL", "Data requested returned NULL, should NOT HAPPEN!");
        } else if (data instanceof Information) {
            return (Information) data;
        } else {
            // This means that answer is manually set.
            return null;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Configuration.getInstance().init(getServletContext()); // must be first!!!

        log = Messenger.getInstance();
        moduleManager = EventModuleManager.getInstance();
        json = JsonConverter.getInstance();
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
        Data check = checkArrival(arrival);
        // If the arrival is valid, checkArrival returns the database user object
        if (check instanceof User) {
            // This means valid session and arrival!
            // Read user, should be used to read rights etc.
            User currentUser = (User) check;
            // First check whether it is a normal task:
            answer = handleNormalTask(arrival, currentUser);
            // If null, it wasn't a normal task – so check if admin rights are set
            if (answer == null && currentUser.isAdmin()) {
                // If yes, handle admin stuff:
                answer = handleAdminTask(arrival, currentUser);
            }
            // If answer is still null, the task wasn't found (or the rights weren't set):
            if (answer == null) {
                log.log(TAG, "Illegal task sent: " + arrival.getTask());
                answer = new Error("POST illegal TASK", "Illegal task: " + arrival.getTask());
            }
        } else {
            // This means the check failed, so there is a message in check that needs to be sent back
            answer = check;
        }
        // Encapsulate answer:
        prepareDeparture(response, answer);
    }

    /**
     * Handles all generally accessible tasks that only require a valid user.
     *
     * @param arrival
     * @return
     */
    // TODO: Place all generally accessible tasks in this method.
    private Data handleNormalTask(Arrival arrival, User user) {
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        switch (task) {
            case ECHO:
                // Simple echo test for checking if the server can parse the data
                return arrival.getObject();
            case USER_READ:
                // TODO: strip password hash? is that necessary?
                return user;
            case USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                User sentUser = (User) arrival.getObject();
                // Email is primary key, thus can not be changed!
                if (!sentUser.getEmail().equals(user.getEmail())) {
                    return new Error("IllegalChange", "Email can not be changed in an existing user");
                }
                // Make sure that you can't set yourself to admin
                if (sentUser.isAdmin()) {
                    return new Error("IllegalChange", "You do not have the rights to modify your permissions!");
                }
                // We need to catch a password change, as it must be hashed:
                String password = sentUser.getPwdHash();
                if (password != null && !password.isEmpty()) {
                    // hash:
                    sentUser.setPwdHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
                }
                // Note that the session user object now needs to be updated. This is done the next time the user
                // sends a request through SanitationModule; it will always get the up to date object from the
                // database.
                return moduleManager.handleTask(Task.User.UPDATE, sentUser);
            case USER_DELETE:
                // To catch some errors, we enforce that no object has been passed along:
                if (arrival.getObject() != null) {
                    // If null...
                    return new Error("IllegalUserRemove", "To delete your user, you must not pass any object along.");
                }
                // Remove session
                moduleManager.handleTask(Task.Sanitation.LOGOUT, user);
                // Otherwise ignore all else, just delete:
                return moduleManager.handleTask(Task.User.DELETE, user);
            case POSITION_FIND:
                // TODO position returns an error, do we even need to check here? Check with Andy!
                return moduleManager.handleTask(Task.Position.FIND, arrival.getObject());
            case TOGGLE_ADMIN:
                // TODO remove this, only for test!
                user.setAdmin(!user.isAdmin());
                return moduleManager.handleTask(Task.User.UPDATE, user);
            default:
                return null;
        }
    }

    /**
     * Handles tasks that are specifically only for the admin class user.
     *
     * @param arrival
     * @return
     */
    // TODO: Place all admin tasks in this method.
    private Data handleAdminTask(Arrival arrival, User user) {
        Task.API task = Task.API.safeValueOf(arrival.getTask());
        switch (task) {
            case USER_ADD:
                // TODO: Input sanitation? Che
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.CREATE, arrival.getObject());
            case USER_UPDATE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.UPDATE, arrival.getObject());
            case USER_DELETE:
                if (!(arrival.getObject() instanceof User)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.User.DELETE, arrival.getObject());
            case LOCATION_READ:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.READ, arrival.getObject());
            case LOCATION_ADD:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                // TODO update area?
                return moduleManager.handleTask(Task.Location.CREATE, arrival.getObject());
            case LOCATION_REMOVE:
                if (!(arrival.getObject() instanceof Location)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Location.DELETE, arrival.getObject());
            case AREA_READ:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.READ, arrival.getObject());
            case AREA_READ_ALL:
                return moduleManager.handleTask(Task.Area.READ_ALL, null);
            case AREA_ADD:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.CREATE, arrival.getObject());
            case AREA_UPDATE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.UPDATE, arrival.getObject());
            case AREA_REMOVE:
                if (!(arrival.getObject() instanceof Area)) {
                    return new Error("WrongObject", "You supplied a wrong object for this task!");
                }
                return moduleManager.handleTask(Task.Area.DELETE, arrival.getObject());
            case ADMIN_READ_ALL:
                Data data = moduleManager.handleTask(Task.User.READ_ALL, null);
                Data msg = checkDataMessage(data);
                if (msg == null && data instanceof DataList) {
                    DataList list = (DataList) data;
                    DataList<User> admins = new DataList<User>();
                    for (Object us : list) {
                        if (!(us instanceof User))
                            continue;
                        if (((User) us).isAdmin())
                            admins.add((User) us);
                    }
                    return admins;
                }
                return msg;
            case ADMIN_ANNIHILATE_AREA:
                log.log(TAG, "Removing all area objects!");
                return moduleManager.handleTask(Task.Area.ANNIHILATE, null);
            case ADMIN_ANNIHILATE_USER:
                log.log(TAG, "Removing all users!");
                return moduleManager.handleTask(Task.User.ANNIHILATE, null);
            default:
                return null;
        }
    }

    /**
     * Creates JSON object out of answer and encapsulates it in the type object.
     *
     * @param response The response where the data will be written.
     * @param answer   The object to attach.
     * @throws IOException
     */
    private void prepareDeparture(HttpServletResponse response, Data answer) throws IOException {
        // Must be done before write:
        response.setCharacterEncoding("UTF-8");
        // If this happens, send back a standard error message.
        if (answer == null) {
            log.error(TAG, "Empty ANSWER! Should never happen!");
            answer = new Error("Empty ANSWER", "Answer does not contain an object! Make sure your request is valid!");
        }
        Departure dep = new Departure(answer);
        response.getWriter().write(json.toJson(dep));
        response.setContentType("application/json");
    }

    /**
     * Takes the HttpServletRequest and returns the correct arrival object filled with JSON goodies.
     *
     * @param request The request to read.
     * @return The Arrival object filled with the data. NULL if not a valid object, checked in SanitationModule, no need for
     * error correction.
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
        Data data = json.fromJson(out);
        if (data instanceof Arrival) {
            return (Arrival) data;
        } else {
            // No need for error handling, that is done in SanitationModule
            return null;
        }
    }

    /**
     * Method that checks whether an arrival is valid and not null, then handles login if applicable and checks the session.
     *
     * @param arrival The Arrival object to check.
     * @return Either an Error or a Message if something is wrong; if everything checks out, then the user object.
     */
    public Data checkArrival(Arrival arrival) {
        if (arrival == null || !arrival.isValid()) {
            // This means something went wrong. Badly.
            return new Error("IllegalPOST", "POST does not conform to API! Keys valid? Values set? Object correct?");
        }
        // Some tasks can be done without login, here are these SanitationModule tasks:
        Task.Sanitation task = Task.Sanitation.safeValueOf(arrival.getTask());
        // If the task is not an error, than it IS a sanitationModule task:
        if (task != Task.Sanitation.ERROR) {
            return moduleManager.handleTask(task, arrival);
        }
        // Otherwise handle it normally:
        else {
            // Everything from here on out MUST be validated via login, so check the session:
            return moduleManager.handleTask(Task.Sanitation.CHECK, arrival);
        }
    }

    /**
     * Arrival class – structure of incomming requests must conform to this class.
     */
    public class Arrival implements Data {
        /**
         * The session hash of the client.
         */
        private String sessionHash;
        /**
         * The API task to do.
         */
        private String task;
        /**
         * Voluntary object with which to work with during a task.
         */
        private Data object;

        public Arrival(String sessionHash, String task, Data object) {
            this.sessionHash = sessionHash;
            this.task = task;
            this.object = object;
        }

        public String getSessionHash() {
            return sessionHash;
        }

        @Override
        /**
         * Automatically generated toString method.
         */
        public String toString() {
            return "Arrival{" +
                    "sessionHash='" + sessionHash + '\'' +
                    ", task='" + task + '\'' +
                    ", object=" + object +
                    '}';
        }

        /**
         * Method that checks if all important values are not null.
         *
         * @return True if all values are set, otherwise false.
         */
        public boolean isValid() {
            return task != null;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public Data getObject() {
            return object;
        }

        public void setObject(Data object) {
            this.object = object;
        }
    }

    /**
     * Wrapper object class for outgoing answers – required because lists for example can not simply be Jsonated.
     */
    public class Departure implements Data {
        /**
         * The data object that is sent.
         */
        private Data object;

        /**
         * Constructor.
         *
         * @param object The object to send.
         */
        public Departure(Data object) {
            this.object = object;
        }

        public Data getObject() {
            return object;
        }

        public void setObject(Data object) {
            this.object = object;
        }
    }

}
