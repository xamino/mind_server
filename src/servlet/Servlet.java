/**
 * @author Tamino Hartmann
 */

package servlet;

import static servlet.Helper.validate;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logger.Log;
import mail.Mailer;

import com.google.gson.Gson;

import database.DatabaseController;
import database.account.Account;
import database.account.AccountController;
import database.offer.Offer;
import database.offer.OfferController;

/**
 * Dieses Servlet ist fuer alle oeffentlich zugaengliche Daten zustaendig.
 * Insbesondere betrifft dies den Abruf aller verfuegbaren Angebote aus der
 * Datenbank fuer den Index.
 */

@WebServlet("/Servlet/*")
public class Servlet extends HttpServlet {

	/**
	 * Private Instanz des Loggers.
	 */
	private Log log;
	/**
	 * Private Instanz des OfferController.
	 */
	private OfferController offController;
	/**
	 * Variable zum speichern der GSON Instanz.
	 */
	private Gson gson;
	/**
	 * Standard serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor.
	 */
	public Servlet() {
		super();
		gson = new Gson();
		log = Log.getInstance();
		offController = OfferController.getInstance();
		log.write("Servlet", "Instance created.");
	}

	/**
	 * Diese Methode handhabt die Abarbeitung von Aufrufen. Normalerweise werden
	 * vom System nur POST-Anfragen kommen, also wird hier auf alle Javascript
	 * aufrufe eingegangen.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		path = (path == null) ? "" : path;
		// log.write("Servlet", "Received request <" + path + ">.");
		if (path.equals("/js/loadOffers")) {
			Vector<Offer> offers = offController.getCheckedOffers();
			// On error:
			if (offers == null) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler in der Datenbank!");
				return;
			}
			// On empty:
			if (offers.isEmpty()) {
				// If no offers are in the DB:
				response.setContentType("text/plain");
				response.getWriter().write("null");
				return;
			}
			// On filled:
			// Replace username with real one (for security & better
			// readability)
			for (Offer off : offers) {
				off.setAuthor(AccountController.getInstance()
						.getAccountByUsername(off.getAuthor()).getName());
			}
			response.setContentType("application/json");
			response.getWriter().write(gson.toJson(offers, offers.getClass()));
			return;
		} else if (path.equals("/js/forgotPassword")) {
			String email = request.getParameter("email");
			// Check validity:
			if (!validate(email)) {
				return;
			}
			// Go for getting the account to this email:
			Account acc = AccountController.getInstance().getAccountByEmail(
					email);
			if (acc == null) {
				log.write("Servlet", "No account found for <" + email + ">");
				return;
			} else if (acc.getUsername().isEmpty()) {
				// In this case, send an explanatory email:
				if (!Mailer
						.getInstance()
						.sendMail(
								email,
								Helper.EMAILHEADER + "Password neu setzen",
								"Für diese Emailaddresse wurde ein neues Password angefordert."
										+ " Da es jedoch mehrere Accounts mit dieser Emailaddresse gibt, "
										+ "wenden sie sich bitte an den Administrator, um ein neues Password anzufordern.")) {
					log.write("Servlet",
							"Error sending mail to multiple accounts.");
				}
				return;
			} else {
				// Send new password:
				log.write("Servlet", "<" + acc.getUsername()
						+ "> requested new password.");
				// Generate new password:
				String newPassword = Long.toHexString(
						Double.doubleToLongBits(Math.random())).substring(0, 8);
				// Generate hash to save to database:
				String encoded = Helper.b64_md5(newPassword);
				// Check to be sure:
				if (encoded == null) {
					log.write(
							"Servlet",
							"ERROR hashing new password, ABORTED! Has md5.js been placed at the correct location?");
					return;
				}
				// Send mail:
				if (!Mailer
						.getInstance()
						.sendMail(
								email,
								Helper.EMAILHEADER + "Neues Passwort",
								"Für diese Emailaddresse wurde ein neues Passwort angefordert."
										+ "\n\nNeues Passwort: "
										+ newPassword
										+ "\n\nBitte vergeben sie möglichst bald ein neues Passwort!")) {
					log.write("Servlet",
							"Error sending mail. Aborting password reset.");
					return;
				}
				// Write new password hash to DB:
				if (!DatabaseController.getInstance().update("Accounts",
						new String[] { "passworthash" },
						new Object[] { encoded },
						"benutzername LIKE '" + acc.getUsername() + "'")) {
					log.write("Servlet",
							"Error updating password in DB! Aborting!");
					return;
				}
				return;
			}
		} else {
			response.sendRedirect(Helper.D_INDEX);
		}
	}

	/**
	 * Diese Methode handhabt die Abarbeitung von Aufrufen. Hier wird nur an die
	 * Indexseite weitergeleitet, da das System normalerweise alles via POST
	 * macht.
	 **/
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.sendRedirect(Helper.D_INDEX);
		} catch (IOException e) {
			// WTF?! Warum kann hier ein IO-Error auftreten?
			e.printStackTrace();
		}
	}
}
