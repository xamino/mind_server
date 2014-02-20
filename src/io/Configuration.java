package io;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Andreas on 20.02.14.
 */
public class Configuration {

    private static Configuration instance;

    private Properties config;
    private ServletContext context;

    private String dbURL;
    private String dbPort;
    private String dbName;
    private String dbDriver;
    private String dbUser;
    private String dbPassword;


    private Configuration() {
        config = new Properties();

        try {
            config.load(this.getClass().getClassLoader().getResourceAsStream(
                    "config.properties")); // TODO does not work!

            dbURL = config.getProperty("DATABASE_URL");
            dbPort = config.getProperty("DATABASE_PORT");
            dbName = config.getProperty("DATABASE_NAME");
            dbDriver = "com.mysql.jdbc.Driver";
            dbUser = config.getProperty("DATABASE_USER");
            dbPassword = config.getProperty("DATABASE_PASSWORD");

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
        return dbURL;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }
}
