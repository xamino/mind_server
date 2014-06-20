package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Saveable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Tamino Hartmann
 */
public class FileLog {
    private static FileLog INSTANCE;
    private final String TAG = "FileLog";
    private final String SEPARATOR;
    private final String OBJECTSFOLDER = "objects";
    private final String LOGFOLDER = "mind_log";
    private final String INFOLOGFILE = "output";
    private final String LOGFILETYPE = ".log";
    private final SimpleDateFormat fileDateFormat;
    private final SimpleDateFormat exactDateFormat;
    private boolean LOG;
    private String PATH;
    private Messenger log;
    private Configuration configuration;

    private FileLog() {
        // get dependent objects
        log = Messenger.getInstance();
        configuration = Configuration.getInstance();
        fileDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        exactDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // get configs
        SEPARATOR = System.getProperty("file.separator");
        LOG = configuration.isFileLogActive();
        PATH = configuration.getFileLogPath();
        // create legal files for us to work on
        getFilesFromPath();
        log.log(TAG, "Path: " + PATH + " Logging: " + LOG);
        log.log(TAG, "Created.");
    }

    public synchronized static FileLog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileLog();
        }
        return INSTANCE;
    }

    /**
     * Overload for when you want to log without a reason.
     *
     * @param toLog The data object to log.
     * @param <E>   The type of the object extending Data.
     */
    public synchronized <E extends Data> void logObject(E toLog) {
        logObject(toLog, "");
    }

    /**
     * Logs a given object to its daily object file.
     *
     * @param toLog  The data object to log.
     * @param <E>    The type of the object extending Data.
     * @param reason A string containing the event type.
     */
    // todo push this in a separate thread
    public synchronized <E extends Data> void logObject(E toLog, final String reason) {
        if (!LOG) {
            return;
        }
        Date today = new Date();
        final String fileName = this.createFileName(fileDateFormat.format(today), toLog.getClass());
        final File objectFile = this.getObjectFile(fileName);
        BufferedWriter writer = null;
        try {
            // tell the writer where to write and if to append if exists ==> true
            writer = new BufferedWriter(new FileWriter(objectFile, true));
            if (reason == null || reason.isEmpty()) {
                writer.write(exactDateFormat.format(today) + " | " + createObjectString(toLog));
            } else {
                writer.write(exactDateFormat.format(today) + " | " + reason + " | " + createObjectString(toLog));
            }
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            log.error(TAG, "Failed to write object to file!");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(TAG, "Failed to close writer!");
                    e.printStackTrace();
                }
            }
        }
        log.log(TAG, "Logged!");
    }

    /**
     * Function where the object is converted to a string. Should be anonymous and case by case.
     *
     * @param object The object to write.
     * @return The string that will be written to the file.
     */
    // todo maybe have objects implement an interface?
    private <E extends Data> String createObjectString(E object) {
        // todo implement correctly etc
        if (object instanceof Saveable) {
            return object.getClass().getSimpleName() + " @ " + ((Saveable) object).getKey();
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    private void getFilesFromPath() {
        // set valid path
        if (PATH == null || PATH.isEmpty()) {
            PATH = System.getProperty("user.home");
            PATH += SEPARATOR + LOGFOLDER;
        }
        // create if nonexistent
        File directory = new File(PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            if (!directory.mkdir()) {
                log.error(TAG, "Failed to create dir at " + PATH);
            }
        }
    }

    private File getObjectFile(String fileName) {
        // create the file name for today, eg "2014_06_22-Poll.log"
        File objectFolder = new File(PATH + SEPARATOR + OBJECTSFOLDER);
        File objectFile = new File(PATH + SEPARATOR + OBJECTSFOLDER + SEPARATOR + fileName);
        if (!objectFile.exists() || !objectFile.isFile()) {
            objectFolder.mkdirs();
            try {
                objectFile.createNewFile();
            } catch (IOException e) {
                log.error(TAG, "Failed to create file " + fileName + "!");
                e.printStackTrace();
                return null;
            }
        }
        return objectFile;
    }

    private String createFileName(String date, Class clazz) {
        return date + "-" + clazz.getSimpleName() + LOGFILETYPE;
    }
}
