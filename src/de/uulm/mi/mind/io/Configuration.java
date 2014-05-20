package de.uulm.mi.mind.io;


import de.uulm.mi.mind.logger.Messenger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Andreas on 20.02.14.
 */
public class Configuration {

    private static Configuration instance;
    private Messenger log;

    private final String CLASS = "Configuration";

    private String dbName;
    private String adminName;
    private String adminEmail;
    private String adminPassword;
    private String universitySSID;
    private ArrayList<String> wifiNameFilter;
    private String dbPort;
    private String dbURL;
    private String dbUser;
    private String dbPassword;


    private Configuration() {
        log = Messenger.getInstance();
    }

    public void init(String servletPath) {

        Properties config = new Properties();
        String customConfigPath = servletPath + "WEB-INF/config.properties";


        if (new File(customConfigPath).exists()) {
            try {
                config.load(new FileInputStream(customConfigPath));

                this.dbName = config.getProperty("DATABASE_NAME");
                this.adminName = config.getProperty("ADMIN_NAME");
                this.adminEmail = config.getProperty("ADMIN_EMAIL");
                this.adminPassword = config.getProperty("ADMIN_PASSWORD");
                this.universitySSID = config.getProperty("UNIVERSITY_SSID");
                this.dbURL = config.getProperty("DATABASE_URL");
                this.dbPort = config.getProperty("DATABASE_PORT");
                this.dbUser = config.getProperty("DATABASE_USER");
                this.dbPassword = config.getProperty("DATABASE_PASSWORD");

                String wifiNames = config.getProperty("UNIVERSITY_SSID_FILTER");
                this.wifiNameFilter = new ArrayList<>(Arrays.asList(wifiNames.split(",")));


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else log.log(CLASS, "Did not find custom config! Loading standard config.");


        // load default config
        try {
            config.load(new FileInputStream(servletPath + "WEB-INF/stdconfig.properties"));

            if (this.dbName == null)
                this.dbName = config.getProperty("DATABASE_NAME");
            if (this.adminName == null)
                this.adminName = config.getProperty("ADMIN_NAME");
            if (this.adminEmail == null)
                this.adminEmail = config.getProperty("ADMIN_EMAIL");
            if (this.adminPassword == null)
                this.adminPassword = config.getProperty("ADMIN_PASSWORD");
            if (this.universitySSID == null)
                this.universitySSID = config.getProperty("UNIVERSITY_SSID");
            if (this.wifiNameFilter == null) {
                String wifiNames = config.getProperty("UNIVERSITY_SSID_FILTER");
                this.wifiNameFilter = new ArrayList<>(Arrays.asList(wifiNames.split(",")));
            }
            if (this.dbURL == null)
                this.dbURL = config.getProperty("DATABASE_URL");
            if (this.dbPort == null)
                this.dbPort = config.getProperty("DATABASE_PORT");
            if (this.dbUser == null)
                this.dbUser = config.getProperty("DATABASE_USER");
            if (this.dbPassword == null)
                this.dbPassword = config.getProperty("DATABASE_PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates and returns a singleton Configuration object
     *
     * @return
     */
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getAdminName() {
        return this.adminName;
    }

    public String getAdminEmail() {
        return this.adminEmail;
    }

    public String getAdminPassword() {
        return this.adminPassword;
    }

    public String getUniversitySSID() {
        return this.universitySSID;
    }

    public ArrayList<String> getWifiNameFilter() {
        return this.wifiNameFilter;
    }

    public String getDbURL() {
        return this.dbURL;
    }

    public String getDbPort() {
        return this.dbPort;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public String getDbPassword() {
        return this.dbPassword;
    }
}