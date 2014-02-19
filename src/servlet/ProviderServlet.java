/**
 * @author Tamino Hartmann
 * @author Laura Irlinger
 * @author Oemer Sahin
 * @author Patryk Boczon
 */
package servlet;

import static servlet.Helper.validate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logger.Log;
import user.Provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import database.offer.Offer;

/**
 * Das <code>Provider</code> Servlet behandelt alle Aktionen von angemeldeten
 * Anbietern / Mitgliedern (Provider).
 */

@WebServlet("/Provider/*")
public class ProviderServlet extends HttpServlet {
	/**
	 * Variable zum speichern der Log Instanz.
	 */
	private Log log;
	/**
	 * Standard serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Variable zum speichern der GSON Instanz.
	 */
	private Gson gson;

	/**
	 * Konstruktor. Hier werden die wichtigen Referenzen gesetzt und wenn noetig
	 * erstellt. Auch wird ein log Eintrag geschrieben um die Initialisierung
	 * ersichtlich zu machen.
	 */
	public ProviderServlet() {
		super();
		log = Helper.log;
		gson = new GsonBuilder().setDateFormat("dd.MM.yyyy").create();
	}

	/**
	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Check authenticity:
		Provider provider = Helper.checkAuthenticity(request.getSession(),
				Provider.class);
		if (provider == null) {
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_INDEX);
			return;
		}
		// Switch action on path:
		String path = request.getPathInfo();
		// Load my offers:
		if (path.equals("/js/loadOffers")) {
			Vector<Offer> myoffers = provider.getOwnOffers();
			response.setContentType("offer/json");
			response.getWriter().write(
					gson.toJson(myoffers, myoffers.getClass()));
		}
		// Delete own account:
		else if (path.equals("/js/deleteAccount")) {
			String username = provider.getUserData().getUsername();
			if (!validate(username)) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler beim parsen von Parametern!");
				return;
			}
			if (provider.deleteOwnAccount()) {
				log.write("ApplicantServlet", username
						+ " has deleted his account.");
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_INDEX);
			} else {
				log.write("ApplicantServlet",
						"There was an error while deleting account with username:"
								+ username);
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim löschen des Accounts!");
			}
		}
		// change own account data
		else if (path.equals("/js/changeAccount")) {
			String name = request.getParameter("name");
			String email = request.getParameter("mail");
			String pw = request.getParameter("pw");
			String rep = request.getParameter("rep");
			// falls leeres pw-> null damit die editOwnAccount
			// funktion das pw nicht auf "" setzt!
			if (pw.equals(""))
				pw = null;
			if (!validate(rep))
				rep = "";
			// pw wird mit absicht nicht überprüft!
			if (!validate(name) || !validate(email)) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der parameter!");
				return;
			}
			if (provider.editOwnAccount(name, email, pw, rep)) {
				log.write("ApplicantServlet", provider.getUserData()
						.getUsername() + " has modified his account.");
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
			} else {
				log.write("ApplicantServlet",
						"There was an error while modifying account with username:"
								+ provider.getUserData().getUsername());
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim ändern der Daten.");
			}
		} else // Do loadAccount:
		if (path.equals("/js/loadAccount")) {
			response.setContentType("application/json");
			response.getWriter().write(provider.getJSONAccountInfo());
		}
		// loads potential representatives for this account
		else if (path.equals("/js/loadRepresentatives")) {
			Vector<String> representatives = provider.getRepresentatives();
			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(representatives, representatives.getClass()));
		}
		// Creates an Vector with all applicants from the selected Offer
		else if (path.equals("/js/applicantChoice")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der AID!");
				return;
			}
			response.setContentType("showtheapplicants/json");
			response.getWriter().write(provider.getApplicants(aid));
			return;
		}
		// Gets the free and total slots of an offer (as String)
		else if (path.equals("/js/getTotalSlots")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der AID!");
				return;
			}
			response.setContentType("showfreeandtotalslots/json");
			response.getWriter().write(provider.getFreeSlotsOufOfTotal(aid));
			return;
		}
		// Creating a new Offer
		else if (path.equals("/js/addOffer")) {
			int stellen = -1;
			double stunden = -1;
			try {
				stellen = Integer.parseInt(request.getParameter("stellen"));
				stunden = Double.parseDouble(request.getParameter("std"));
			} catch (NumberFormatException e) {
				// System.out.println("ERROR WHILE PARSING DOUBLE IN ProviderServlet");
				log.write("ProviderServlet",
						"There was an error while PARSING double-value(stellen) in: "
								+ path.toString());
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler beim Parsen! Kein/ungueltiger Wert eingegeben [INT Wert von 'Stellen' pruefen]");
				return;
			}
			String name = request.getParameter("titel");
			String notiz = request.getParameter("notiz");
			String beschreibung = request.getParameter("beschreibung");
			String startDateS = request.getParameter("startDate");
			String endDateS = request.getParameter("endDate");

			// Check der Werte:
			if (!validate(name) || !validate(notiz) || !validate(beschreibung)
					|| stellen == -1 || stunden == -1) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der Parameter!");
				return;
			}

			Date startDate;
			try {
				startDate = new SimpleDateFormat("dd-MM-yyyy")
						.parse(startDateS);
			} catch (Exception e) {
				log.write("ProviderServlet",
						"There was an error while PARSING StartDate");
				response.setContentType("text/error");
				response.getWriter().write("invalid startDate");
				return;
			}
			Date endDate;
			try {
				endDate = new SimpleDateFormat("dd-MM-yyyy").parse(endDateS);
			} catch (Exception e) {
				log.write("ProviderServlet",
						"There was an error while PARSING EndDate");
				response.setContentType("text/error");
				response.getWriter().write("invalid endDate");
				return;
			}
			if (startDate.after(endDate) && !endDate.equals(startDate)) {
				log.write("ClerkServlet", "StartDate after Enddate!");
				response.setContentType("text/error");
				response.getWriter().write("order");
				return;
			}
			if (!provider.createOffer(name, notiz, stellen, stunden,
					beschreibung, startDate, endDate)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim erstellen des Angebots in der Datenbank!");
				return;
			}
			log.write("ProviderServlet", "Angebot <" + name + "> erstellt.");
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
			return;
		}
		// Angebot zurueckziehen
		else if (path.equals("/js/deleteOffer")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim Parsen der AID!");
				return;
			}
			if (!provider.deleteOffer(aid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim löschen des Angebots in der Datenbank!");
				return;
			}
			log.write("ProviderServlet", "Angebot <" + aid + "> gelöscht.");
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
			return;

		}
		// Loads selected Offer into the Form elements
		else if (path.equals("/js/getOffer")) {
			int aid;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim Parsen der AID!");
				return;
			}
			Offer offtoup = provider.getOffer(aid);
			// Happens if provider may not edit this offer (not his):
			if (offtoup == null) {
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
				return;
			}
			response.setContentType("application/json");
			response.getWriter().write(gson.toJson(offtoup, Offer.class));
			return;
		}
		// Saves changes from selected Offer and updates it in the db
		else if (path.equals("/js/updateOffer")) {
			int aid;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim Parsen der AID!");
				return;
			}
			String titel = request.getParameter("titel");
			String description = request.getParameter("beschreibung");
			if (!validate(titel) || !validate(description) || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der Parameter!");
				return;
			}
			if (!provider.updateOffer(aid, titel, description)) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler beim aktualisieren des Angebots in der Datenbank!");
				return;
			}
			log.write("ProviderServlet", "Angebot <" + aid + "> aktualisiert.");
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
			return;

		}
		// Sets the boolean value of "ausgewaehlt" in the db table
		// "berwerbungen" to "true"
		else if (path.equals("/js/takeSelectedApplicant")) {
			String username = request.getParameter("usernameTakenApplicant");
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim Parsen der AID!");
				return;
			}
			if (!validate(username) || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen der Parameter!");
				return;
			}
			if (!provider.selectApplicant(aid, username)) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler beim Annehmen eines Bewerbers!\nBewerber "
								+ "bereits aufgenommen oder keine Stellen mehr frei!");
				return;
			}
			log.write("ProviderServlet", "Bewerber <" + username + "> für <"
					+ aid + "> angenommen.");
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_PROVIDER_USERINDEX);
			return;
		}
		// Path for reading standard values from db for creating new offers.
		else if (path.equals("/js/getDefValues")) {
			String obj = provider.readDefValues();
			if (obj == null || obj.isEmpty()) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler in der Datenbank!\nWerte konnten nicht ausgelesen werden.");
				return;
			}
			response.setContentType("application/json");
			response.getWriter().write(obj);
			return;
		} else {
			log.write("ProviderServlet", "Unknown path <" + path + ">");
		}
	}
}