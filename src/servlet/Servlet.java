/**
 * @author Tamino Hartmann
 */

package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DatabaseController;

/**
 * Dieses Servlet ist fuer alle oeffentlich zugaengliche Daten zustaendig.
 * Insbesondere betrifft dies den Abruf aller verfuegbaren Angebote aus der
 * Datenbank fuer den Index.
 */

@WebServlet("/Servlet/*")
public class Servlet extends HttpServlet {

	public Servlet() {
		super();
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
		// todo – switch on path string
        response.getWriter().println("Hello there! This is the server speaking!");
	}

    /**
     * This method handles all simple data requests.
     * @param request
     * @param response
     */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
        // todo
	}
}
