/**
 * @author Tamino Hartmann
 */

package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.json.JsonWrapper;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.Departure;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.managers.TaskManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Main REST API servlet class of the server. All requests go through here, are validated, parsed, and processed.
 */
public class Servlet extends HttpServlet {

    /**
     * TAG for logging.
     */
    private final String TAG = "Servlet";
    /**
     * JSON library.
     */
    private JsonWrapper json;
    /**
     * Class for logging stuff.
     */
    private Messenger log;

    @Override
    public void init() throws ServletException {
        // init all
        super.init();
        log = Messenger.getInstance();
        json = JsonWrapper.getInstance();
        log.log(TAG, "Created.");
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
        // Start timer
        // log.pushTimer(this, "INCOMING");
        // Get arrival object
        // Watch out, arrival.getData() might be NULL!
        Arrival arrival = getRequest(request);

        Sendable answer = null;
        if (arrival == null) {
            answer = new Error(Error.Type.CAST, "Failed to read Arrival!");
        } else {
            // set IP address in case we need it (warning: can be IPv4 OR IPv6!!!)
            arrival.setIpAddress(request.getRemoteAddr());

            long time = System.currentTimeMillis();
            answer = TaskManager.getInstance().run(arrival);
            log.log(TAG, "Task " + arrival.getTask() + " took " + (System.currentTimeMillis()-time) + "ms.");
        }

        // Encapsulate answer:
        prepareDeparture(response, answer);

        // TimerResult timerResult = log.popTimer(this);
        // log.error(TAG, "Request " + arrival.getTask() + " took " + timerResult.time + " ms.");
    }

    /**
     * Takes the HttpServletRequest and returns the correct arrival object filled with JSON goodies.
     *
     * @param request The request to read.
     * @return The Arrival object filled with the data. NULL if not a valid object, checked in SecurityModule, no need for
     * error correction.
     * @throws IOException
     */
    private Arrival getRequest(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        BufferedReader reader = request.getReader();
        StringBuilder stringBuffer = new StringBuilder();
        do {
            String value = reader.readLine();
            if (value == null || value.isEmpty())
                break;
            stringBuffer.append(value);
        } while (true);
        String out = stringBuffer.toString();
        // Better safe than sorry:
        if (out.isEmpty()) {
            return null;
        }
        // catch object:null
        if (out.contains(",\"object\":null")) {
            out = out.replace(",\"object\":null", "");
        }
        // parse the object out:
        Data data = json.fromJson(out);
        if (data instanceof Arrival) {
            return (Arrival) data;
        } else {
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
    private void prepareDeparture(HttpServletResponse response, Sendable answer) throws IOException {
        // Must be done before write:
        response.setCharacterEncoding("UTF-8");
        // If this happens, send back a standard error message.
        if (answer == null || answer instanceof None) {
            log.error(TAG, "Empty ANSWER! Should never happen!");
            answer = new Error(Error.Type.WRONG_OBJECT, "Answer does not contain an object! Make sure your request is valid!");
        }
        Departure dep = new Departure(answer);
        String jsonBack = json.toJson(dep);
        response.getWriter().write(jsonBack);
        response.setContentType("application/json");
    }
}
