/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import database.DatabaseController;
import database.objects.Error;
import io.Configuration;
import logger.Messenger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private SanitationSecurity sanSec;

    @Override
    public void init() throws ServletException {
        super.init();

        gson = new Gson();
        log = Messenger.getInstance();
        sanSec = SanitationSecurity.getInstance();

        Configuration.getInstance().init(getServletContext());
        DatabaseController.getInstance();
    }

    /**
     * This method receives all json requests that will result in a write or modify of objects.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        path = (path == null) ? "" : path;
        // todo
        sanSec.secure();
        // Write whatever you want sent back to this object:
        Object answer = null;
        switch (path) {
            default:
                log.log(TAG, "Unknown path sent: " + path);
                answer = new Error("404", "Unknown path: <" + path + ">");
                break;
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
        String path = request.getPathInfo();
        path = (path == null) ? "" : path;
        // todo
        sanSec.secure();
        // Write whatever you want sent back to this object:
        Object answer = null;
        switch (path) {
            default:
                log.log(TAG, "Unknown path sent: " + path);
                answer = new Error("404", "Unknown path: " + path);
                break;
        }
        prepareDeparture(response, answer);
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
}
