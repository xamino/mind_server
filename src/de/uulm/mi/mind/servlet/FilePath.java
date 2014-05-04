package de.uulm.mi.mind.servlet;

import javax.servlet.ServletContext;

/**
 * @author Tamino Hartmann
 *         <p/>
 *         Class that makes sure that all Servlets work on the same directory for all file operations.
 */
public class FilePath {

    private String filePath;
    private String SEP = System.getProperty("file.separator");

    public FilePath(ServletContext context) {
        // set base directory for files to be stored in
        filePath = context.getRealPath("/") + "images" + SEP;
    }

    public String mapPath() {
        return filePath;
    }

    public String iconPath() {
        return filePath + SEP + "custom_icons" + SEP;
    }
}
