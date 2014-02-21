package io;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
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

                instance.dbURL = config.getProperty("DATABASE_URL");
                instance.dbPort = config.getProperty("DATABASE_PORT");
                instance.dbName = config.getProperty("DATABASE_NAME");
                instance.dbDriver = config.getProperty("DATABASE_DRIVER");
                instance.dbUser = config.getProperty("DATABASE_USER");
                instance.dbPassword = config.getProperty("DATABASE_PASSWORD");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else log.log(CLASS, "Did not find custom config! Loading standard config.");


        // load default config
        try {
            config.load(context.getResourceAsStream(
                    "WEB-INF/stdconfig.properties"));

            if (instance.dbURL == null)
                instance.dbURL = config.getProperty("DATABASE_URL");
            if (instance.dbPort == null)
                instance.dbPort = config.getProperty("DATABASE_PORT");
            if (instance.dbName == null)
                instance.dbName = config.getProperty("DATABASE_NAME");
            if (instance.dbDriver == null)
                instance.dbDriver = config.getProperty("DATABASE_DRIVER");
            if (instance.dbUser == null)
                instance.dbUser = config.getProperty("DATABASE_USER");
            if (instance.dbPassword == null)
                instance.dbPassword = config.getProperty("DATABASE_PASSWORD");
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
        return instance.dbURL;
    }

    public String getDbDriver() {
        return instance.dbDriver;
    }

    public String getDbUser() {
        return instance.dbUser;
    }

    public String getDbPassword() {
        return instance.dbPassword;
    }

    public String getDbPort() {
        return instance.dbPort;
    }

    public String getDbName() {
        return instance.dbName;
    }
}
