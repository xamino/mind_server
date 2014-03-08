package io;

import logger.Messenger;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Andreas on 20.02.14.
 */
public class Configuration {

    private static Configuration instance;

    private String dbURL;
    private String dbPort;
    private String dbName;
    private String dbDriver;
    private String dbUser;
    private String dbPassword;
    private Messenger log;
    private final String CLASS = "Configuration";


    private Configuration() {

        log = Messenger.getInstance();
    }

    public void init(ServletContext context) {

        Properties config = new Properties();

        InputStream customConfig = context.getResourceAsStream(
                "WEB-INF/config.properties");
        if (customConfig != null) {
            try {
                config.load(customConfig);

                this.dbURL = config.getProperty("DATABASE_URL");
                this.dbPort = config.getProperty("DATABASE_PORT");
                this.dbName = config.getProperty("DATABASE_NAME");
                this.dbDriver = config.getProperty("DATABASE_DRIVER");
                this.dbUser = config.getProperty("DATABASE_USER");
                this.dbPassword = config.getProperty("DATABASE_PASSWORD");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else log.log(CLASS, "Did not find custom config! Loading standard config.");


        // load default config
        try {
            config.load(context.getResourceAsStream(
                    "WEB-INF/stdconfig.properties"));

            if (this.dbURL == null)
                this.dbURL = config.getProperty("DATABASE_URL");
            if (this.dbPort == null)
                this.dbPort = config.getProperty("DATABASE_PORT");
            if (this.dbName == null)
                this.dbName = config.getProperty("DATABASE_NAME");
            if (this.dbDriver == null)
                this.dbDriver = config.getProperty("DATABASE_DRIVER");
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

    public String getDbURL() {
        return this.dbURL;
    }

    public String getDbDriver() {
        return this.dbDriver;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public String getDbPassword() {
        return this.dbPassword;
    }

    public String getDbPort() {
        return this.dbPort;
    }

    public String getDbName() {
        return this.dbName;
    }
}
