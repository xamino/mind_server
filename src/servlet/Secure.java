/**
 * @author Tamino Hartmann
 * @author Laura Irlinger
 * @author Manuel Guentzel
 */
package servlet;

import static servlet.Helper.validate;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import logger.Log;
import user.User;
import userManagement.LoggedInUsers;
import userManagement.UserFactory;
import database.account.Account;
import database.account.AccountController;

/**
 * Das <code>Secure</code> Servlet behandelt den Login der Benutzer, die
 * Registrierung von neuen Bewerbern, und der Logout von Benutzern abgearbeitet.
 */

@WebServlet("/Secure/*")
public class Secure extends HttpServlet {
	/**
	 * Standard serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Private Instanz des Loggers. Alle Systemausgaben werden an den Logger
	 * gegeben.
	 */
	private Log log;

	/**
	 * Konstruktor.
	 */
	public Secure() {
		super();
		this.log = Helper.log;
	}

	/**
	 * Diese Methode handhabt die Abarbeitung von Aufrufen. Diese werden
	 * normalerweise von Javascript ausgeloest.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		// Makes sure that no NullPointerExceptions are thrown...
		path = (path == null) ? "" : path;
		// Only use to debug path:
		// log.write("Secure", "Received request <" + path + ">.");
		// If login is asked:
		if (path.equals("/js/doLogin")) {
			String userName = request.getParameter("userName");
			String userPassword = request.getParameter("userPassword");
			if (!validate(userName) || !validate(userPassword)) {
				response.setContentType("text/error");
				response.getWriter().write("Error parsing parameters!");
				return;
			}
			// log.write("Secure", "Checking login: <" + userName + ">:<"+
			// userPassword + ">");
			Account acc = AccountController.getInstance().getAccountByUsername(
					userName);
			if (acc == null) {
				response.setContentType("text/plain");
				response.getWriter().write("false");
				// log.write("Secure",
				// "Login failed: Wrong username or password");
			} else if (!(userPassword.equals(acc.getPasswordhash()))) {
				response.setContentType("text/plain");
				response.getWriter().write("false");
				// log.write("Secure",
				// "Login failed: Wrong username or password");
			} else {
				// log.write("Secure", "Login successful");
				HttpSession session = request.getSession();
				session.setAttribute("userName", new String(userName));
				session.setMaxInactiveInterval(15 * 60);
				UserFactory.getUserInstance(acc, session);
				int type = acc.getAccounttype();
				response.setContentType("text/url");
				// Switch according to accounttype:
				if (type == 0)
					response.getWriter().write(Helper.D_ADMIN_USERINDEX);
				if (type == 1)
					response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
				if (type == 2)
					response.getWriter().write(Helper.D_CLERK_USERINDEX);
				if (type == 3)
					response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
			}

		}
		// If register is asked:
		else if (path.equals("/js/doRegister")) {
			// First get parameters:
			String realName = request.getParameter("realName");
			String email = request.getParameter("email");
			String userName = request.getParameter("userName");
			String password = request.getParameter("userPassword");
			// Check for null or empty (against hacks& errors)
			if (!validate(realName) || !validate(email) || !validate(userName)
					|| !validate(password)) {
				response.setContentType("text/error");
				response.getWriter().write("Error parsing parameters!");
				return;
			}
			log.write("Secure", "Registering <" + realName + "> as <"
					+ userName + ">.");
			Account acc = AccountController.getInstance().getAccountByUsername(
					userName);
			if (acc == null) {
				acc = new Account(userName, password, 3, email, realName, 0,
						null);
				if (!AccountController.getInstance().createAccount(acc)) {
					response.setContentType("text/error");
					response.getWriter().write(
							"DB error on server on creation of account!");
					return;
				}
				log.write("Secure", "Registration successful.");
				HttpSession session = request.getSession();
				session.setAttribute("userName", new String(userName));
				session.setMaxInactiveInterval(15 * 60);
				UserFactory.getUserInstance(acc, session);
				// Write url to response:
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
			} else {
				response.setContentType("text/plain");
				response.getWriter().write("used");
				// log.write("Secure","Registration failed! Username already used!");
			}
			return;
		}
		// If logout is asked:
		else if (path.equals("/js/doLogout")) {
			HttpSession session = request.getSession();
			User user = LoggedInUsers.getUserBySession(session);
			// Abfrage da evtl user gar nicht eingeloggt wurde:
			if (user != null)
				user.invalidate();
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_INDEX);
			return;
		}
		// If unknown path:
		else {
			log.write("Secure", "Unknown operation!");
			// response.sendRedirect(Helper.D_INDEX);
		}
	}
}
