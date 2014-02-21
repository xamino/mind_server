package database;

import database.objects.Area;
import database.objects.Location;
import database.objects.User;
import io.Configuration;
import logger.Messenger;

import javax.servlet.ServletContext;
import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.jar.Manifest;

/**
 * @author Tamino Hartmann
 */
public class DatabaseController {
    /**
     * Variable for storing the instance of the class.
     */
    private static DatabaseController instance;
    private final Messenger log;
    private final String CLASS = "DatabaseController";
    private String url;

    /**
     * Verbingung zur Datenbank
     */
    private Connection con;
    /**
     * Statement zum ausfuerhen von SQL Befehlen
     */
    private Statement st;
    /**
     * Username fuer den Datenbankzugriff
     */
    private String user;
    /**
     * Passwort fuer den Datenbankzugriff
     */
    private String password;
    /**
     * Datenbankname
     */
    private String database;
    /**
     * Portnummer
     */
    private String port;

    /**
     * Method for getting a valid reference of this object.
     *
     * @return Instance of DatabaseController.
     */
    public static DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    /**
     * Private constructor for DatabaseController for implementing the singleton
     * instance. Use getInstance() to get a reference to an object of this type.
     */
    private DatabaseController() {
        log = Messenger.getInstance();
        Configuration config = Configuration.getInstance();

        try {
            Class.forName(config.getDbDriver()).newInstance();
            // getLoginInfo();
            url = config.getDbURL();
            user = config.getDbUser();
            password = config.getDbPassword();
            database = config.getDbName();
            port = config.getDbPort();

            if (password != null) {
                log.log(CLASS, "Try login: "
                        + "jdbc:mysql://" + url + ":" + port + "/" + database
                        + "?user=" + user + "&password=" + password);
                con = DriverManager.getConnection("jdbc:mysql://" + url + ":"
                        + port + "/" + database + "?user=" + user
                        + "&password=" + password);
            } else {
                log.log(CLASS, "Try login: "
                        + "jdbc:mysql://" + url + ":" + port + "/" + database
                        + "?user=" + user);
                con = DriverManager.getConnection("jdbc:mysql://" + url + ":"
                        + port + "/" + database + "?user=" + user);
            }
            st = con.createStatement();
        } catch (Exception e) {
            log.log(
                    CLASS,
                    "Error while connecting to database: please check if DB is running and if logindata is correct!");
            log.log(CLASS, "Try login: "
                    + "jdbc:mysql://" + url + ":" + port + "/" + database
                    + "?user=" + user + "&password=" + password);

            // Commented out by Tamino (it was making me edgy... :D )
            // e.printStackTrace();
        }
    }


    /**
     * Methode welche ein SQL "update" Statement ausfuehrt.
     *
     * @param table   Name der Tabelle.
     * @param columns Name der Spalten welche aktualisiert werden sollen.
     * @param values  Daten, welche in die entsprechenden Spalten gefuehlt werden
     *                sollen.
     * @param where   Bedingung fuer die Aktualisierung.
     * @return <code>True</code> wenn erfolgreich, sonst <code>false</code>.
     */
    synchronized public boolean update(Data data) {
        // Sicherheitsüberprüfung:
        if (con == null) {
            return false;
        }

        String table;
        String[] columns;
        Object[] values;
        String where;
        String update;

        if (data instanceof User) {
            User user = (User) data;
            table = "user";
            columns = new String[]{"name", "password", "email"};
            values = new Object[]{user.getName(), user.getPwdHash(), user.getEmail()};
            where = "email='" + user.getEmail() + "'";

            update = "UPDATE " + table + " SET "
                    + commanator(columns, values) + " WHERE " + where;
        } else return false;


        try {
            st.executeUpdate(update);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Methode welche ein SQL "delete" Statement ausfuehrt.
     *
     * @param table Tabelle aus der ein Eintrag bzw. mehrere Eintraege geloescht werden soll.
     * @param where 'WHERE' Bedingung.
     * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
     */
    synchronized public boolean delete(String table, String where) {
        // Sicherheitsüberprüfung:
        if (con == null) {
            return false;
        }
        String del = "DELETE FROM " + table + " WHERE " + where;
        try {
            st.executeUpdate(del);
            return true;
        } catch (SQLException e) {
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Methode welche ein SQL "insert" Statement ausfuehrt.
     *
     * @param table  Name der Tabelle.
     * @param values Einzufuegende Werte.
     * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
     */
    synchronized public boolean insert(String table, Object[] values) {
        // Sicherheitsüberprüfung:
        if (con == null) {
            return false;
        }
        String insert = "INSERT INTO " + table + " VALUES ("
                + commanator(values) + ")";
        try {
            st.executeUpdate(insert);
            return true;
        } catch (SQLException e) {
            // Commented out because this error happens often and should be
            // handled further up.
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Gibt die Anzahl der Zeilen einer Tabelle aus, die die "Where"- Bedingung
     * erfuellen
     *
     * @param from  Tabellen, die in die Suche miteinbezogen werden sollen
     * @param where Zu erfuellende Bedingung
     * @return Anzahl der Zeilen
     */
    synchronized public int count(String[] from, String where) {
        // Sicherheitsüberprüfung:
        if (con == null) {
            return 0;
        }
        String sel = "SELECT COUNT(*) FROM " + commanator(from);
        if (where != null)
            sel += " WHERE " + where;
        ResultSet rs;
        try {
            rs = st.executeQuery(sel);
            rs.next();
            return rs.getInt("COUNT(*)");
        } catch (SQLException e) {

        }
        return 0;
    }

    /**
     * Methode welche ein SQL "select" Statement ausfuehrt.
     *
     * @param select Welche Werte ausgewaehlt werden sollen.
     * @param from   Namen der Tabellen.
     * @param where  Zusaetzliche Bedingung. Wird keine benoetigt, kann
     *               <code>null</code> gesetzt werden.
     * @return Gibt ein <code>ResultSet</code> mit den Antworddaten zurueck.
     */
    synchronized public ResultSet select(String[] select,
                                    String[] from,
                                    String where) {
        // Sicherheitsüberprüfung:
        if (con == null) {

            return null;
        }

        String sel = "SELECT " + commanator(select) + " FROM "
                + commanator(from);
        if (where != null)
            sel += " WHERE " + where;
        ResultSet rs;
        // System.out.println(sel);
        try {
            rs = st.executeQuery(sel);
            return rs;
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return null;
    }

    /**
     * Methode welche ein SQL "insert on not null update" Statement ausfuehrt.
     *
     * @param table   Name der Tabelle.
     * @param columns Namen der Spalten.
     * @param values  Ensprechende Werte welche eingefuegt oder aktualiesiert werden
     *                sollen.
     * @return boolean Bei TRUE erfolgreich ausgefuehrt. Sonst FALSE.
     */
    synchronized public boolean insertOnNullElseUpdate(String table,
                                                       String[] columns, Object[] values) {
        // Sicherheitsüberprüfung:
        if (con == null) {

            return false;
        }
        String update = "INSERT INTO " + table + " VALUES ("
                + commanator(values) + ") ON DUPLICATE KEY UPDATE "
                + commanator(columns, values);
        try {
            st.executeUpdate(update);
            return true;
        } catch (SQLException e) {

        }
        return false;
    }

    /**
     * Hilfsmethode zum Konkatenieren von Strings mit Kommasetzung.
     *
     * @param stringz Zu konkatenierende Strings.
     * @return Konkatenierter String.
     */
    private String commanator(String[] stringz) {
        String ret = "";
        for (int i = 0; i < stringz.length; i++) {
            ret += stringz[i];
            if (i != (stringz.length - 1))
                ret += ", ";
        }
        return ret;
    }

    /**
     * Hilfsmethode zum Konkatenieren von Objekten mit Kommasetzung. Dabei
     * werden Strings mit einfachen Anfuehrungszeichen eingeklammert.
     *
     * @param objects Zu konkatenierende objekte.
     * @return Konkatenierter String.
     */
    private String commanator(Object[] objects) {
        String ret = "";
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof String)
                // Correctly display strings:
                ret += "'" + objects[i] + "'";
            else if (objects[i] instanceof Date) {
                // Correctly parse & enter dates:
                ret += "'" + new java.sql.Date(((Date) objects[i]).getTime())
                        + "'";
            } else
                ret += objects[i];
            if (i != (objects.length - 1))
                ret += ", ";
        }
        return ret;
    }

    /**
     * Hilfsmethode zum konkatenieren von zwei String arrays fuer update
     * statements. Ergibt in etwa einen String der Form
     * "name[0]=value[0], name...".
     *
     * @param name  Namen der Spalten.
     * @param value Werte, welche die Spalten bekommen sollen.
     * @return String mit zusammengesetzten Inhalt.
     */
    private String commanator(String[] name, Object[] value) {
        String ret = "";
        if (name.length != value.length)
            return "ERROR IN COMMANATOR!";
        for (int i = 0; i < name.length; i++) {
            ret += name[i] + "=";
            if (value[i] instanceof String)
                ret += "'" + value[i] + "'";
            else if (value[i] instanceof Date) {
                // Correctly parse & enter dates:
                ret += "'" + new java.sql.Date(((Date) value[i]).getTime())
                        + "'";
            } else
                ret += value[i];
            if (i != (name.length - 1))
                ret += ", ";
        }
        return ret;
    }
}
