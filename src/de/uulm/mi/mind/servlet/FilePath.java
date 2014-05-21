package de.uulm.mi.mind.servlet;

import de.uulm.mi.mind.logger.Messenger;

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
        if (context == null) {
            // TAMINO FIX IF THIS HAPPENS
            Messenger.getInstance().error("FilePath","Context is null! THIS WON'T WORK!");
        }
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
