/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import database.Data;
import database.objects.*;
import database.objects.Error;
import io.Configuration;
import logger.Messenger;
import logic.EventModuleManager;
import logic.modules.UserModule;

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

        gson = new Gson();
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
     * This method handles all simple data requests.
     *
     * @param request
     * @param response
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        User u = new User("Hans", "hans@peter.de");
        moduleManager.handleTask(Task.UserTask.CREATE_USER, u);

        User b = (User) moduleManager.handleTask(Task.UserTask.READ_USER, u);

        b.setName("Gustav");

        moduleManager.handleTask(Task.UserTask.UPDATE_USER, b);

        moduleManager.handleTask(Task.UserTask.DELETE_USER, b);
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
    private void prepareDeparture(HttpServletResponse response, Object answer) throws IOException {
        // Must be done before write:
        response.setCharacterEncoding("UTF-8");
        // If this happens, send back a standard error message.
        if (answer == null) {
            answer = new Error("Empty ANSWER", "Answer does not contain an object!");
        }
        String returnJson = "{\"dataType\":\"" + answer.getClass().toString().split(" ")[1] + "\",\"object\":"
                + gson.toJson(answer, answer.getClass()) + "}";
        response.getWriter().write(returnJson);
        response.setContentType("application/json");
    }

    /**
     * Takes the HttpServletRequest and returns the correct arrival object filled with JSON goodies.
     *
     * @param request The request to read.
     * @return The Arrival object filled with the data.
     * @throws IOException
     */
    // TODO: will not work with recursive data objects – how do we implement that?
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
        String dataType = getJSONValue("dataType", out);
        String dataString = getJSONValue("object", out);
        // If a dataType is given, try parsing it:
        Data object = null;
        if (dataType != null && !dataType.isEmpty()) {
            // Parse data
            // TODO this might be doable with reflection API?
            // TODO possibly use full object path, as is sent by server? "User" --> "database.objects.User"?
            switch (dataType) {
                case "Message":
                    object = gson.fromJson(dataString, Message.class);
                    break;
                case "User":
                    object = gson.fromJson(dataString, User.class);
                    break;
                case "Error":
                    object = gson.fromJson(dataString, Error.class);
                    break;
                case "Success":
                    object = gson.fromJson(dataString, Success.class);
                    break;
                default:
                    log.log(TAG, "ERROR: Unknown data sent, can not be parsed into Arrival!");
            }
        }
        return new Arrival(getJSONValue("sessionHash", out), getJSONValue("task", out), dataType, object);
    }

    /**
     * Given the JSON key, returns the corresponding value out of the given JSON object.
     *
     * @param key    The JSON key to get.
     * @param object The string of the JSON object.
     * @return The value associated with the key. If non is found, set to null.
     */
    private String getJSONValue(final String key, String object) {
        int j = object.indexOf("\"" + key + "\"");
        // This happens if the key isn't found
        if (j == -1) {
            log.log(TAG, "WARNING JSON key not found \"" + key + "\"");
            return null;
        }
        int i = j + key.length() + 1;
        String retValue = null;
        if (object.charAt(i + 2) == '"') {
            // Simple data with '
            retValue = object.substring(i + 2).split("\"")[1];
        } else if (object.charAt(i + 2) == '\'') {
            // Simple data with "
            retValue = object.substring(i + 2).split("'")[1];
        } else if (object.charAt(i + 2) == '{') {
            // Data with {} – note that the surrounding braces are kept!
            retValue = object.substring(i + 2).split("}")[0] + "}";
        } else {
            log.log(TAG, "ERROR parsing ARRIVAL for key \"" + key + "\"");
        }
        // If the value is empty, we return null
        if (retValue == null || retValue.isEmpty())
            return null;
        return retValue;
    }
}
