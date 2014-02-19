/**
 * @author Laura Irlinger
 * @author Tamino Hartmann
 * @author Patryk Boczon
 * @author Oemer Sahin
 */
package servlet;

import static servlet.Helper.validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import logger.Log;
import user.Clerk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import database.HilfsDatenClerk;
import database.account.Account;
import database.document.Document;
import database.offer.Offer;

/**
 * Das <code>Clerk</code> Servlet behandelt alle Aktionen von angemeldeten
 * (Sach-)Bearbeitern (Clerk).
 */

@WebServlet("/Clerk/*")
public class ClerkServlet extends HttpServlet {
	/**
	 * Standard serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Variable zum speichern der Log Instanz.
	 */
	private Log log;
	// /**
	// * Variable zum speichern einer Instanz vom Mailer
	// */

	/**
	 * Variable zum speichern der GSON Instanz.
	 */
	private Gson gson;

	/**
	 * Konstruktor. Hier werden die wichtigen Referenzen gesetzt und wenn noetig
	 * erstellt. Auch wird ein log Eintrag geschrieben um die Initialisierung
	 * ersichtlich zu machen.
	 */
	public ClerkServlet() {
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
		Clerk clerk = Helper.checkAuthenticity(request.getSession(),
				Clerk.class);
		if (clerk == null) {
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_INDEX);
			return;
		}
		String path = request.getPathInfo();
		path = (path == null) ? "" : path;
		// log.write("ClerkServlet", "Received request: " + path);
		// Load the offers of the clerk:
		if (path.equals("/js/showMyOffers")) {
			// Load all correct offers:
			Vector<Offer> myoffers = clerk.getAllUncheckedOffers();
			response.setContentType("offers/json");
			response.getWriter().write(
					gson.toJson(myoffers, myoffers.getClass()));
		} else if (path.equals("/js/editOneOffer")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				log.write("ClerkServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter().write("Fehler! Ungültige AID:");
				return;
			}
			// AID should be != -1 here, so continue:
			Offer offertoedit = clerk.getOfferByAID(aid);
			// Ist das Angebot vom gleichen Institut?
			if (offertoedit == null) {
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_CLERK_OFFERMANAGEMENT);
				return;
			}
			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(offertoedit, offertoedit.getClass()));
			return;
		} else if (path.equals("/js/saveOffer")) {
			boolean changed = Boolean.parseBoolean(request
					.getParameter("changed"));
			boolean accepted = Boolean.parseBoolean(request
					.getParameter("annehmen"));
			int aid = Integer.parseInt(request.getParameter("aid"));
			double hoursperweek = Double.parseDouble(request
					.getParameter("hoursperweek"));
			double wage = 0.0;
			try {
				wage = Double.parseDouble(request.getParameter("wage"));
			} catch (NumberFormatException e) {
				log.write("ClerkServlet",
						"NumberFormatException while parsing URL!");
				response.setContentType("text/error");
				response.getWriter()
						.write("Fehler bei Eingabe! Nur double Werte erlaubt fuer wage.");
				return;
			}
			// Read dates:
			String startDate = request.getParameter("startDate");
			String endDate = request.getParameter("endDate");
			if (!validate(startDate) || !validate(endDate)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler bei Eingabe! Datum nicht lesbar!");
				return;
			}
			Offer offertosave = clerk.getOfferByAID(aid);
			// note: is done in OfferController, no need to set twice...
			// set modificationdate to current date
			// java.util.Date aenderungsdatum = new java.util.Date();
			// java.sql.Date aenderungsdatum_toUp = new java.sql.Date(
			// aenderungsdatum.getTime());
			// sets modificationdate and updates it
			// offertosave.setModificationdate(aenderungsdatum_toUp);
			offertosave.setWage(wage);
			offertosave.setHoursperweek(hoursperweek);
			// write dates to offer:
			try {
				SimpleDateFormat x = new SimpleDateFormat("dd-MM-yyyy");
				x.setLenient(false);
				offertosave.setStartdate(x.parse(startDate));
			} catch (ParseException e) {
				log.write("ClerkServlet",
						"There was an error while PARSING StartDate");
				response.setContentType("text/error");
				response.getWriter().write("invalid startDate");
				return;
			}
			try {
				SimpleDateFormat x = new SimpleDateFormat("dd-MM-yyyy");
				x.setLenient(false);
				offertosave.setEnddate(x.parse(endDate));
			} catch (ParseException e) {
				log.write("ClerkServlet",
						"There was an error while PARSING EndDate");
				response.setContentType("text/error");
				response.getWriter().write("invalid endDate");
				return;
			}

			if (offertosave.getStartdate().after(offertosave.getEnddate())
					&& !offertosave.getEnddate().equals(
							offertosave.getStartdate())) {
				log.write("ClerkServlet", "StartDate after Enddate!");
				response.setContentType("text/error");
				response.getWriter().write("order");
				return;
			}
			// logic for checked:
			if (changed && accepted) {
				offertosave.setChecked(true);
				offertosave.setFinished(false);
			} else if (changed && !accepted) {
				offertosave.setChecked(false);
				offertosave.setFinished(true);
			} else {
				offertosave.setChecked(false);
				offertosave.setFinished(false);
			}
			// System.out.println(offertosave);
			if (!clerk.updateOffer(offertosave)) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim update der Datenbank!");
				return;
			}
			log.write("ClerkServlet", "<" + clerk.getUserData().getUsername()
					+ "> changed offer <" + offertosave.getAid() + ">");
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_CLERK_OFFERMANAGEMENT);
			return;
		} else if (path.equals("/js/documentsFromOffer")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen von AID!");
				return;
			}
			Vector<Document> documents = clerk.getDocumentsFromOffer(aid);
			response.setContentType("documentsoffer/json");
			response.getWriter().write(
					gson.toJson(documents, documents.getClass()));

		}
		// Creates a Vector of Documents which can be added to an offer
		else if (path.endsWith("/js/documentsToAddToOffer")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen von AID!");
				return;
			}
			Vector<Document> docsToAdd = clerk.getUnusedDocForOffer(aid);
			response.setContentType("documentstoaddoffer/json");
			response.getWriter().write(
					gson.toJson(docsToAdd, docsToAdd.getClass()));
		}
		// Creates a Vector of Documents which can be added to an application
		else if (path.endsWith("/js/documentsToAddToApplication")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen von AID!");
				return;
			}
			String username = request.getParameter("username");
			if (!validate(username) || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler in den Parametern!");
				return;
			}
			Vector<Document> docsToAdd = clerk.getDocsForApplication(aid,
					username);
			response.setContentType("docstoaddtoapp/json");
			response.getWriter().write(
					gson.toJson(docsToAdd, docsToAdd.getClass()));
		} else if (path.equals("/js/showApplication")) {
			Account clerkAccount = clerk.getAccount();
			Vector<HilfsDatenClerk> daten = clerk.getVoodoo(clerkAccount);
			if (daten == null || daten.isEmpty()) {
				response.setContentType("text/plain");
				response.getWriter().write(
						"Aktuell gibt es keine zu bearbeitende Bewerbungen!");
				return;
			}
			response.setContentType("showapplication/json");
			response.getWriter().write(gson.toJson(daten, daten.getClass()));
		} else if (path.equals("/js/applicationDocuments")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("AID"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen von AID!");
				return;
			}
			String user = request.getParameter("User");
			if (!validate(user) || aid == -1) {
				// System.out.println("Fehler in den Parametern!");
				response.setContentType("text/error");
				response.getWriter().write("Fehler in den Parametern!");
				return;
			}
			Vector<Object> customDocs = clerk.doVoodoo2nd(aid, user);
			response.setContentType("showthedocuments/json");
			response.getWriter().write(
					gson.toJson(customDocs, customDocs.getClass()));
		}
		// Updates the status of an AppDocument
		else if (path.equals("/js/setDocCheck")) {
			String username = request.getParameter("username");
			int offerid = -1;
			int docid = -1;
			try {
				offerid = Integer.parseInt(request.getParameter("offerid"));
				docid = Integer.parseInt(request.getParameter("docid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von OfferID oder DocID!");
				return;
			}
			if (!validate(username) || offerid == -1 || docid == -1) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (!clerk.updateAppDoc(username, offerid, docid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim updaten von AppDoc in der Datenbank!");
				return;
			}
			log.write("ClerkServelt", "<" + clerk.getUserData().getUsername()
					+ "> changed AppDoc.");
			return;
		}
		// Creates an String for the table in editapplication.jsp
		else if (path.equals("/js/getApplicantInfo")) {
			String user = request.getParameter("User");
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("AID"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim parsen von AID!");
				return;
			}
			if (!validate(user) || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			response.setContentType("application/json");
			response.getWriter().write(clerk.getApplicantInfo(aid, user));
		} else if (path.equals("/js/deleteAppDocument")) {
			int uid = -1;
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von AID oder UID!");
				return;
			}
			String username = request.getParameter("user");
			if (!validate(username) || aid == -1 || uid == -1) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (!clerk.deleteAppDoc(username, aid, uid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim löschen von AppDoc in der Datenbank!");
				return;
			}
			return;
		}
		// Delete own account:
		else if (path.equals("/js/deleteAccount")) {
			String name = request.getParameter("name");
			if (!validate(name)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (clerk.deleteOwnAccount()) {
				log.write("ApplicantServlet", name
						+ " has deleted his account.");
				// Simply now for debugging:
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_INDEX);
			} else {
				response.setContentType("text/error");
				response.getWriter().write("Error while deleting account!");
			}
		}
		// change own account data
		else if (path.equals("/js/changeAccount")) {
			String name = request.getParameter("name");
			String email = request.getParameter("mail");
			String pw = request.getParameter("pw");
			String rep = request.getParameter("rep");
			// System.out.println("clerk pw: " + pw);
			if (pw.equals("")) {
				// falls leeres pw-> null damit die editOwnAccount
				// funktion das pw nicht auf "" setzt!
				pw = null;
			}
			if (rep == "null")
				rep = "";
			// pw wird mit absicht nicht geprüft!
			if (!validate(name) || !validate(email)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (clerk.editOwnAccount(name, email, pw, rep)) {
				log.write("ClerkServlet", clerk.getUserData().getUsername()
						+ " has modified his account.");
				response.setContentType("text/url");
				response.getWriter().write(Helper.D_CLERK_USERINDEX);
			} else {
				response.setContentType("text/error");
				response.getWriter().write("Fehler beim ändern der Daten.");
			}
		}
		// Do loadAccount:
		else if (path.equals("/js/loadAccount")) {
			response.setContentType("application/json");
			response.getWriter().write(clerk.getJSONAccountInfo());
		}
		// loads potential representatives for this account
		else if (path.equals("/js/loadRepresentatives")) {
			Vector<String> representatives = clerk.loadRepresentatives();
			response.setContentType("application/json");
			response.getWriter().write(
					gson.toJson(representatives, representatives.getClass()));
		} else if (path.equals("/js/doApplicationCompletion")) {
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("AID"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Error while parsing String into int");
				return;
			}
			String username = request.getParameter("username");
			if (!validate(username) || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (clerk.checkAllDocFromApplicant(username, aid)) {
				response.setContentType("text/url");
				response.getWriter()
						.write(Helper.D_CLERK_APPLICATIONMANAGEMENT);
			} else {
				response.setContentType("error/url");
				response.getWriter().write(
						"Unvollstaendige Dokumente. Abschluss nicht moeglich");
			}
			return;
		}
		// Funktion zum entfernen eines OfferDocuments des gewaehlten Offers
		else if (path.equals("/js/deleteOfferDocument")) {
			int uid = -1;
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von AID oder UID!");
				return;
			}
			if (!clerk.deleteOfferDoc(uid, aid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim löschen eines OfferDocuments!");
				return;
			}
			return;
		}
		// Funktion zum hinzufuegen eines OfferDocuments des gewaehlten Offers
		else if (path.equals("/js/addOfferDocument")) {
			int uid = -1;
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				log.write("ClerkServlet",
						"Error add offer document! UID or AID invalid!");
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von AID oder UID!");
				return;
			}
			if (!clerk.addOfferDoc(uid, aid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim erstellen eines OfferDocuments!");
				return;
			}
			return;
		}
		// Funktion zum hinzufuegen eines AppDocuments der gewaehlten
		// Application
		else if (path.equals("/js/addAppDocument")) {
			int uid = -1;
			int aid = -1;
			try {
				aid = Integer.parseInt(request.getParameter("aid"));
				uid = Integer.parseInt(request.getParameter("uid"));
			} catch (NumberFormatException e) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von AID oder UID!");
				return;
			}
			String username = request.getParameter("username");
			if (!validate(username) || uid == -1 || aid == -1) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim parsen von den Parametern!");
				return;
			}
			if (!clerk.addAppDoc(username, uid, aid)) {
				response.setContentType("text/error");
				response.getWriter().write(
						"Fehler beim erstellen eines AppDocuments!");
				return;
			}
			return;
		} else if (path.equals("/js/loadInfo")) {
			response.setContentType("application/json");
			response.getWriter().write(clerk.getOfferInfo());
			return;
		}
		// Get email addresses if required:
		else if (path.equals("/js/getEmail")) {
			String user = request.getParameter("user");
			if (!Helper.validate(user)) {
				response.setContentType("text/error");
				response.getWriter().write("Invalid user parameter!");
				return;
			}
			response.setContentType("text/email");
			response.getWriter().write(clerk.getEmail(user));
		} else {
			log.write("ClerkServlet", "Unknown path <" + path + ">");
		}
	}

	/**
	 * GET-Aufruffe werden hier nur zum Download der Excel-Datei verwendet.
	 * Ansonsten leitet es an public/index.jsp weiter.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Check authenticity:
		Clerk clerk = Helper.checkAuthenticity(request.getSession(),
				Clerk.class);
		if (clerk == null) {
			response.setContentType("text/url");
			response.getWriter().write(Helper.D_INDEX);
			return;
		}
		String path = request.getPathInfo();
		if (path.equals("/js/doExcelExport")) {
			File file = null;
			try {
				file = clerk.doExport();
			} catch (RowsExceededException e) {
				response.setContentType("text/error");
				response.getWriter().write("RowsExceededExcetion!");
			} catch (WriteException e) {
				response.setContentType("text/error");
				response.getWriter().write("Error while writing File");
			}
			FileInputStream fileToDownload = new FileInputStream(file);
			ServletOutputStream output = response.getOutputStream();
			response.setContentType("application/msexcel");
			response.setHeader("Content-Disposition",
					"attachment; filename=excelExport.xls");
			response.setContentLength(fileToDownload.available());
			int c;
			while ((c = fileToDownload.read()) != -1) {
				output.write(c);
			}
			output.flush();
			output.close();
			fileToDownload.close();
		}
	}
}
