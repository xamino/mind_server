/**
 * @author Tamino Hartmann
 */

package servlet;

import com.google.gson.Gson;
import logger.Messenger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dieses Servlet ist fuer alle oeffentlich zugaengliche Daten zustaendig.
 * Insbesondere betrifft dies den Abruf aller verfuegbaren Angebote aus der
 * Datenbank fuer den Index.
 */

@WebServlet("/Servlet/*")
public class Servlet extends HttpServlet {

    /**
     * JSON library.
     */
    private Gson gson;
    /**
     * Class for logging stuff.
     */
    private Messenger log;
    /**
     * TAG for logging.
     */
    private final String TAG = "Servlet";

	public Servlet() {
		super();
        gson = new Gson();
        log = Messenger.getInstance();
	}

    /**
     * This method receives all json requests that will result in a write or modify of objects.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		path = (path == null) ? "" : path;
		switch (path) {
            case "":
                break;
            default:
                log.log(TAG, "Unknown path sent: "+path);
                break;
        }
	}

    /**
     * This method handles all simple data requests.
     * @param request
     * @param response
     */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        path = (path == null) ? "" : path;
        switch (path) {
            case "":
                break;
            default:
                log.log(TAG, "Unknown path sent: "+path);
                break;
        }
	}
}
