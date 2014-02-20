/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import database.objects.Error;
import logger.Messenger;

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

    public Servlet() {
        super();
        gson = new Gson();
        log = Messenger.getInstance();
        sanSec = SanitationSecurity.getInstance();
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
        // maintenance (must be called before any writes!):
        response.setCharacterEncoding("UTF-8");
        // ---
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
        // If this happens, send back a standard error message.
        if (answer == null) {
            answer = new Error("NULL PUSH", "Your PUSH did not return an object!");
        }
        response.getWriter().write(gson.toJson(answer, answer.getClass()));
        response.setContentType("application/json");
    }

    /**
     * This method handles all simple data requests.
     *
     * @param request
     * @param response
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        // maintenance (must be called before any writes!):
        response.setCharacterEncoding("UTF-8");
        // ---
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
        // If this happens, send back a standard error message.
        if (answer == null) {
            answer = new Error("NULL GET", "Your GET did not return an object!");
        }
        response.getWriter().write(gson.toJson(answer, answer.getClass()));
        response.setContentType("application/json");
    }
}
