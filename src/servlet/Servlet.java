/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import database.Data;
import database.DatabaseController;
import database.objects.Arrival;
import database.objects.Error;
import database.objects.Message;
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

        gson = new Gson();
        log = Messenger.getInstance();
        sanitation = Sanitation.getInstance();
        moduleManager = EventModuleManager.getInstance();

        Configuration.getInstance().init(getServletContext());
        DatabaseController.getInstance();
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
        Arrival arrival = getRequest(request);
        // Write whatever you want sent back to this object:
        Data answer = null;
        // Check if valid request:
        if (arrival == null) {
            answer = new Error("Illegal POST", "POST does not conform to API!");
            // log.log(TAG, "Illegal format.");
        } else if (!sanitation.checkSession(arrival.getSessionHash())) {
            // login only available when logged out
            if (arrival.getTask().equals("login")) {
                answer = new Message(sanitation.createSession());
            } else {
                answer = new Error("POST Authentication FAIL", "You seem not to be logged in!");
                // log.log(TAG, "Failed login.");
            }
        } else {
            log.log(TAG, "Handling POST.");
            String task = arrival.getTask();
            switch (task) {
                case "logout":
                    sanitation.destroySession(arrival.getSessionHash());
                    answer = new Message("Logged out!");
                    break;
                case "datatest":
                    answer = new Message("called datatest");
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
            answer = new Error("Empty GET", "GET did not return an object!");
        }
        String returnJson = "{\"type\":\"" + answer.getClass().toString().split(" ")[1] + "\",\"object\":"
                + gson.toJson(answer, answer.getClass()) + "}";
        response.getWriter().write(returnJson);
        response.setContentType("application/json");
    }

    // TODO
    private Arrival getRequest(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        String out = "";
        do {
            String value = reader.readLine();
            if (value == null || value.isEmpty())
                break;
            out += value;
        } while (true);

        // todo finish! :P @tamino
        System.out.println("HERE: "+getJSONValue("task",out));

        Arrival arrival = new Arrival("","","",generateDataObject("",""));
        return arrival;
    }

    private Data generateDataObject(String type, String jsonObject) {
        return new Message("");
    }

    private String getJSONValue(String key, String object) {
        int i = object.indexOf(key)+key.length();
        if (object.charAt(i+3)=='"') {
            return object.substring(i+3).split("\"")[1];
        } else if (object.charAt(i+3)=='{') {
            return object.substring(i+3).split("}")[1];
        } else
            return "";
    }
}
