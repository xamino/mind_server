/**
 * @author Tamino Hartmann
 */

package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.EventModuleManager;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Main REST API servlet class of the server. All requests go through here, are validated, parsed, and processed.
 */

@WebServlet("/Servlet")
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
    private EventModuleManager moduleManager;
    private ServletFunctions functions;

    @Override
    public void init() throws ServletException {
        super.init();
        log = Messenger.getInstance();
        moduleManager = EventModuleManager.getInstance();
        json = JsonConverter.getInstance();
        functions = ServletFunctions.getInstance();
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
        /*
        // Start timer
        log.pushTimer(this, "INCOMING");
        */
        // Get arrival object
        // Watch out, arrival.getData() might be NULL!
        Arrival arrival = getRequest(request);
        // Check if valid:
        Data check = checkArrival(arrival);
        // Write whatever you want sent back to this object:
        Data answer = null;
        // If the task was CHECK we don't need to do anything else
        if (!(check instanceof Information) && Task.Security.safeValueOf(arrival.getTask()) == Task.Security.CHECK) {
            // Avoid sending the user object
            answer = functions.checkDataMessage(check, ActiveUser.class);
            if (answer == null) {
                answer = new Success("Your session is valid!");
            }
            // answer shouldn't be null here!
        }
        // If the arrival is valid, checkArrival returns the ActiveUser object
        else if (check instanceof ActiveUser && ((ActiveUser) check).getAuthenticated() instanceof User) {
            // This means valid session and arrival!
            // Read user, should be used to read rights etc.
            User currentUser = (User) ((ActiveUser) check).getAuthenticated();
            // First check whether it is a normal task:
            answer = functions.handleNormalTask(arrival, (ActiveUser) check);
            // If null, it wasn't a normal task – so check if admin rights are set
            if (answer == null && currentUser.isAdmin()) {
                // If yes, handle admin stuff:
                answer = functions.handleAdminTask(arrival, (ActiveUser) check);
            }
            // If answer is still null, the task wasn't found (or the rights weren't set):
            if (answer == null) {
                log.log(TAG, "Illegal task sent: " + arrival.getTask());
                String error = "Illegal task: " + arrival.getTask();
                if (!currentUser.isAdmin()) {
                    error += ". You may not have the necessary rights!";
                }
                answer = new Error(Error.Type.TASK, error);
            }
            // finally we need to ensure that the activeUser is updated in the session list
            Data msg = updateActiveUser(((ActiveUser) check));
            if (msg instanceof Error) {
                answer = msg;
            }
            // otherwise answer is valid
        } else if (check instanceof ActiveUser && ((ActiveUser) check).getAuthenticated() instanceof PublicDisplay) {
            answer = functions.handleDisplayTask(arrival, (ActiveUser) check);
            if (answer == null) {
                log.log(TAG, "Illegal task sent: " + arrival.getTask());
                answer = new Error(Error.Type.TASK, "Illegal task: " + arrival.getTask() + ".");
            }
            // finally we need to ensure that the activeUser is updated in the session list
            Data msg = updateActiveUser(((ActiveUser) check));
            if (msg instanceof Error) {
                answer = msg;
            }
            // otherwise answer is valid
        } else {
            // This means the check failed, so there is a message in check that needs to be sent back
            answer = check;
        }

        // Encapsulate answer:
        prepareDeparture(response, answer);
        /*
        TimerResult timerResult = log.popTimer(this);
        log.error(TAG, "Request " + arrival.getTask() + " took " + timerResult.time + " ms.");
        */
    }

    /**
     * Small helper function. For now it will only update lastPosition!
     *
     * @param activeUser
     * @return
     */
    private Information updateActiveUser(ActiveUser activeUser) {
        return (Information) moduleManager.handleTask(Task.Security.UPDATE, activeUser);
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
        BufferedReader reader = request.getReader();
        String out = "";
        do {
            String value = reader.readLine();
            if (value == null || value.isEmpty())
                break;
            out += value;
        } while (true);
        // Better safe than sorry:
        if (out.isEmpty()) {
            return null;
        }
        // catch object:null
        if (out.contains(",\"object\":null")) {
            out = out.replace(",\"object\":null", "");
            System.out.println(out);
        }
        // parse the object out:
        Data data = json.fromJson(out);
        if (data instanceof Arrival) {
            return (Arrival) data;
        } else {
            // No need for error handling, that is done in SecurityModule
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
            return new Error(Error.Type.SECURITY, "POST does not conform to API! Keys valid? Values set? Object correct?");
        }
        // Some tasks can be done without login, here are these SecurityModule tasks:
        Task.Security task = Task.Security.safeValueOf(arrival.getTask());
        // If the task is not an error, than it IS a sanitationModule task:
        if (task != Task.Security.ERROR) {
            ActiveUser temp = new ActiveUser(((Authenticated) arrival.getObject()), 0, arrival.getSessionHash());
            return moduleManager.handleTask(task, temp);
        }
        // Otherwise handle it normally:
        else {
            // Everything from here on out MUST be validated via login, so check the session:
            return moduleManager.handleTask(Task.Security.CHECK, new ActiveUser(null, 0, arrival.getSessionHash()));
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
            answer = new Error(Error.Type.WRONG_OBJECT, "Answer does not contain an object! Make sure your request is valid!");
        }
        Departure dep = new Departure(answer);
        String blub = json.toJson(dep);
        response.getWriter().write(blub);
        response.setContentType("application/json");
    }
}
