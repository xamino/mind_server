/**
 * @author Tamino Hartmann
 * @author Laura Irlinger
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
import user.Admin;

import com.google.gson.Gson;

import database.account.Account;
import database.document.Document;
import database.institute.Institute;

/**
 * Das <code>Admin</code> Servlet behandelt alle Aktionen von angemeldeten
 * Administratoren.
 */

@WebServlet("/Admin/*")
public class AdminServlet extends HttpServlet {
	/**
	 * Standard serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Variable zum speichern der Log Instanz.
	 */
	private Log log;
	/**
	 * Variable zum speichern der GSON Instanz.
	 */
	private Gson gson;

	/**
	 * Konstruktor des AdminServlet. Hier werden die wichtigen Referenzen
	 * gesetzt und wenn noetig erstellt. Auch wird ein log Eintrag geschrieben
	 * um die Initialisierung ersichtlich zu machen.
	 **/
	public AdminServlet() {
		super();
		log = Helper.log;
		gson = new Gson();
		log.write("AdminServlet", "Instance created.");
	}

	/**
	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Check authenticity:
		Admin admin = Helper.checkAuthenticity(request.getSession(),
				Admin.class);
		if (admin == null) {
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_INDEX);
			return;
		}
		// Switch action on path:
		String path = request.getPathInfo();
		// Only activate this if you need to debug the path:
		// log.write("AdminServlet", "Received request <" + path+">.");
		if (path.equals("/js/loadAccounts")) {
			Vector<Account> accounts = admin.getAccounts();
			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(accounts, accounts.getClass()));
		}
		// Delete an account:
		else if (path.equals("/js/deleteAccount")) {
			// Get username parameter:
			String username = request.getParameter("name");
			// Check if legal:
			if (!validate(username)) {
				log.write("AdminServlet", "Username invalid!");
				response.setContentType("text/error");
				response.getWriter().write("Username invalid!");
				return;
			}
			// Do & check if all okay:
			if (!admin.deleteAccount(username)) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Dieser Benutzer existiert nicht oder ist aktuell angemeldet. Kann nicht gelöscht werden!");
			}

		}
		// Get the information of an account:
		else if (path.equals("/js/getAccountData")) {
			String username = request.getParameter("name");
			if (!validate(username)) {
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_ADMIN_ACCOUNTSMANAGEMENT);
				return;
			}
			log.write("AdminServlet", "Getting data of account with username <"
					+ username + ">");
			Account account = admin.getUserAccount(username);
			response.setContentType("application/json");
			response.getWriter().write(gson.toJson(account, Account.class));
		} else if (path.equals("/js/addAccount")) {
			// /hiwi/Admin/js/addAccount?realName=&email=&userName=&userPassword=&accountType=&institute=
			String realName = request.getParameter("realName");
			String email = request.getParameter("email");
			String userName = request.getParameter("userName");
			String password = request.getParameter("userPassword");
			int accountType = -1;
			int institute = -1;
			try {
				institute = Integer.parseInt(request.getParameter("institute"));
				accountType = Integer.parseInt(request
						.getParameter("accountType"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für Institut und AccountType.");
				return;
			}
			if (!validate(realName) || !validate(email) || !validate(userName)
					|| !validate(password) || accountType < 0
					|| accountType > 3 || institute == -1) {
				log.write("AdminServlet", "Error in parameters!");
				response.setContentType("text/error");
				response.getWriter().write("Werte illegal!");
				return;
			}
			// If already exists:
			if (admin.getUserAccount(userName) != null) {
				log.write("AdminServlet",
						"Error creating account – username alreay exists!");
				response.setContentType("text/plain");
				response.getWriter().write("false");
				return;
			}
			// Okay, all okay, continue:
			Account account = new Account(userName, password, accountType,
					email, realName, institute, null);
			if (!admin.createAccount(account)) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Account konnte nicht erstellt werden! Existiert das Institut in der Datenbank?");
				return;
			}
			response.setContentType("text/plain");
			response.getWriter().write("true");
			return;
		} else if (path.equals("/js/getSystemInformation")) {
			String info= admin.getSysteminfo();
			response.setContentType("application/json");
			response.getWriter().write(info);
		} else if (path.equals("/js/editAccount")) {
			String realName = request.getParameter("realName");
			String email = request.getParameter("email");
			String userName = request.getParameter("userName");
			String password = request.getParameter("userPassword");
			// System.out.println(password);
			int institute = -1;
			int accountType = -1;
			try {
				institute = Integer.parseInt(request.getParameter("institute"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für Institut.");
				return;
			}
			if (!validate(realName) || !validate(userName) || !validate(email)
					|| institute == -1) {
				log.write("AdminServlet", "Error in parameters!");
				response.setContentType("text/error");
				response.getWriter().write("Werte illegal!");
				return;
			}
			// This can happen and is legal if the password isn't to be changed:
			Account useracc = admin.getUserAccount(userName);
			if (!validate(password))
				password = useracc.getPasswordhash();
			accountType = useracc.getAccounttype();
			String representative = useracc.getRepresentative();
			if (!admin.editAccount(new Account(userName, password, accountType,
					email, realName, institute, representative))) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim Update in der Datenbank!");
				return;
			}
			response.setContentType("text/plain");
			response.getWriter().write("true");
			return;
		} else if (path.equals("/js/loadDocuments")) {
			Vector<Document> documents = admin.getDocuments();
			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(documents, documents.getClass()));
		} else if (path.equals("/js/addDocument")) {
			String title = request.getParameter("title");
			String description = request.getParameter("description");
			int uid = -1;
			try {
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				// Note: As of here, all errors that can happen regularly are
				// encoded with a number in the text/error response so that the
				// client can display the correct error message at the correct
				// location in the webpage.
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("0");
				return;
			}
			if (!validate(title) || !validate(description) || uid < 0) {
				log.write("AdminServlet", "Error in parameters!");
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler bei Eingabe! Fehlende Eingaben.");
				return;
			}
			// all okay... continue:
			if (!admin.addDoc(new Document(uid, title, description))) {
				response.setContentType("text/error");
				response.getWriter().write("1");
				return;
			}
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
			return;
		} else if (path.equals("/js/deleteDocument")) {
			int uid = -1;
			try {
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("Fehlerhafte UID!");
				return;
			};
			if(!admin.deleteDoc(uid)){
				log.write("AdminServlet",
						"Error deleting document!");
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim Löschen des Dokuments");
			}
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
			return;
		} else if (path.equals("/js/getDocument")) {
			int uid = -1;
			try {
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("Fehlerhafte UID!");
				return;
			}
			Document doc = admin.getSpecificDocument(uid);
			response.setContentType("application/json");
			response.getWriter().write(gson.toJson(doc, Document.class));
			return;
		} else if (path.equals("/js/editDocument")) {
			String title = request.getParameter("title");
			String description = request.getParameter("description");
			int uid = -1;
			try {
				uid = Integer.parseInt(request.getParameter("UID"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler bei Eingabe! Nur ganze Zahlen erlaubt für die UID.");
				return;
			}
			if (!validate(title) || !validate(description) || uid < 0) {
				log.write("AdminServlet", "Error in parameters!");
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler bei Eingabe! Fehlende Eingaben.");
				return;
			}
			// all okay... continue:
			if (!admin.editDoc(new Document(uid, title, description))) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim edititieren des Dokuments!");
				return;
			}
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_ADMIN_DOCUMENTSMANAGEMENT);
			return;
		}
		// Load institutes as JSON data string:
		else if (path.equals("/js/loadInstitutes")) {
			Vector<Institute> inst =admin.getInstitutes();
			if (inst == null) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim laden der Institute!");
				return;
			}
			// for (Institute in : inst)
			// System.out.println(in.getIID() + ":" + in.getName());
			response.setContentType("application/json");
			response.getWriter().write(gson.toJson(inst, inst.getClass()));
			return;
		}
		// Add an institute to the DB:
		else if (path.equals("/js/addInstitute")) {
			String name = request.getParameter("name");
			int IID = -1;
			try {
				IID = Integer.parseInt(request.getParameter("IID"));
			} catch (NumberFormatException e) {
				// Note: As of here, all errors that can happen regularly are
				// encoded with a number in the text/error response so that the
				// client can display the correct error message at the correct
				// location in the webpage.
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("0");
				return;
			}
			if (!validate(name) || IID < 0) {
				log.write("AdminServlet", "Error in parameters!");
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler bei Eingabe des Institutes! Fehlende Eingaben.");
				return;
			}
			// all okay... continue:
			if (!admin.addInstitute(new Institute(IID, name))) {
				response.setContentType("text/error");
				response.getWriter().write("1");
				return;
			}
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_ADMIN_INSTITUTESMANAGMENT);
			return;
		}
		// Delete an institute from the DB:
		else if (path.equals("/js/deleteInstitute")) {
			int IID = -1;
			try {
				IID = Integer.parseInt(request.getParameter("IID"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("Fehlerhafte IID!");
				return;
			}
			Institute institute = admin.getSpecificInstitute(IID);
			if (!admin.deleteInstitute(institute)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim löschen! Stimmt die IID?");
				return;
			}
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_ADMIN_INSTITUTESMANAGMENT);
			return;
		}
		// Read default values from DB:
		else if (path.equals("/js/getDefValues")) {
			String obj = admin.readDefValues();
			if (obj == null || obj.isEmpty()) {
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler in der Datenbank!\nWerte konnten nicht ausgelesen werden.");
				return;
			}
			response.setContentType("application/json");
			response.getWriter().write(obj);
			return;
		}
		// Write default values to DB:
		else if (path.equals("/js/saveDefValues")) {
			int hoursMonth = -1;
			float wage = -1;
			try {
				hoursMonth = Integer.parseInt(request
						.getParameter("hoursMonth"));
				wage = Float.parseFloat(request.getParameter("wage"));
			} catch (NumberFormatException e) {
				log.write("AdminServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("Fehlerhafte Eingaben!");
				return;
			}
			String startDateS = request.getParameter("startDate");
			String endDateS = request.getParameter("endDate");
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
			if (startDate.after(endDate)
					&& !endDate.equals(
							startDate)) {
				log.write("ClerkServlet", "StartDate after Enddate!");
				response.setContentType("text/error");
				response.getWriter().write("order");
				return;
			}
			if (hoursMonth == -1
					|| wage == -1) {
				response.setContentType("text/error");
				response.getWriter().write("Invalid parameters!");
				return;
			}
			// System.out.println(hoursMonth + ":" + startDate + ":" + endDate);
			if (!admin.writeDefValues(hoursMonth, wage, startDate, endDate)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Error saving new values to database!");
				return;
			}
			log.write("AdminServlet", "<" + admin.getUserData().getUsername()
					+ "> edited default offer values.");
			response.setContentType("text/plain");
			response.getWriter().write("true");
			return;
		}
		// Unknown:
		else {
			log.write("AdminServlet", "Unknown path <" + path + ">");
		}
	}
}
