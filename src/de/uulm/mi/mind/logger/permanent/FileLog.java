package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Tamino Hartmann
 *         Class that writes lines of log to files. Files are written to a folder per day.
 */
public class FileLog {
    private static FileLog INSTANCE;
    private final String TAG = "FileLog";
    private final String PATHSEPARATOR;
    private final SimpleDateFormat fileDateFormat;
    private final SimpleDateFormat exactDateFormat;
    private boolean LOG;
    private boolean ASYNC;
    private String PATH;
    private Messenger log;

    /**
     * Private constructor. Builds directories, sets paths, prepares it all.
     */
    private FileLog() {
        // get dependent objects
        log = Messenger.getInstance();
        Configuration configuration = Configuration.getInstance();
        fileDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        exactDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // get configs
        PATHSEPARATOR = System.getProperty("file.separator");
        LOG = configuration.isFileLogActive();
        PATH = configuration.getFileLogPath();
        ASYNC = configuration.getFileLogAsync();
        // set valid path
        if (PATH == null || PATH.isEmpty()) {
            PATH = System.getProperty("user.home");
            PATH += PATHSEPARATOR + "mind_log";
        }
        // create if nonexistent
        File directory = new File(PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            if (!directory.mkdir()) {
                log.error(TAG, "Failed to create dir at " + PATH);
            }
        }
        // and we're done
        if (LOG) {
            log.log(TAG, "Created. Running " + (ASYNC ? "asynchronously (faster)" : "inline (slower)") + " at " + PATH + ".");
        } else {
            // warn in case this is not wanted
            log.error(TAG, "Created. Warning: not running!");
        }
    }

    public synchronized static FileLog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileLog();
        }
        return INSTANCE;
    }

    /**
     * Helper function that appends the given line to the given file. Will not create a file that doesn't exist!
     *
     * @param file The file to try to append the line. If it doesn't exist return false.
     * @param line The line of text to append.
     * @return Whether it worked or not.
     */
    private boolean writeFile(File file, String line) {
        boolean allOkay = true;
        // if the file doesn't exist we break immediately
        if (!file.exists()) {
            return false;
        }
        BufferedWriter writer = null;
        try {
            // tell the writer where to write and if to append if exists ==> true
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            log.error(TAG, "Failed to write object to file!");
            e.printStackTrace();
            allOkay = false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(TAG, "Failed to close writer!");
                    e.printStackTrace();
                    allOkay = false;
                }
            }
        }
        return allOkay;
    }

    /**
     * Given a file name gets the Java File object for it.
     *
     * @param fileName The file name to get, creating it if needed, including the folder structure to it.
     * @return The File object if successful, else null.
     */
    private File getObjectFile(String fileName) {
        final String DIRTODAY = fileDateFormat.format(new Date());
        // get dir for today
        File objectFolder = new File(PATH + PATHSEPARATOR + DIRTODAY);
        // get file at dir for today
        File objectFile = new File(PATH + PATHSEPARATOR + DIRTODAY + PATHSEPARATOR + fileName);
        if (!objectFile.exists() || !objectFile.isFile()) {
            if (!objectFolder.mkdirs()) {
                // return null if not successful
                return null;
            }
            try {
                if (!objectFile.createNewFile()) {
                    // return null if not successful
                    return null;
                }
            } catch (IOException e) {
                log.error(TAG, "Failed to create file " + fileName + "!");
                e.printStackTrace();
                return null;
            }
        }
        return objectFile;
    }

    /**
     * Small helper function for consistingly creating sensible file names.
     *
     * @param date The formatted date string to write.
     * @param name The name of the file to write.
     * @return The built string.
     */
    private String createFileName(String date, String name) {
        return date + "-" + name + ".log";
    }

    /**
     * Function for logging. Takes a LogWorker which will return the LogObject for actually logging. If ASYNC is false,
     * logging will happen synchronously – otherwise the function will return after starting a thread, pushing the task
     * of creating the LogObject off the main thread, thus freeing it quickly to continue its work.
     *
     * @param worker The LogWorker that will create the LogObject to log.
     */
    public void log(LogWorker worker) {
        // no need to do all the work if we're not logging, oui?
        if (!LOG) {
            return;
        }
        // create the runnable
        LogThread execute = new LogThread(worker);
        // run in thread if ASYNC, else run in line
        if (ASYNC) {
            new Thread(execute).start();
        } else {
            execute.run();
        }
    }

    /**
     * Class for asynchronously writing the log to a file.
     */
    private class LogThread implements Runnable {
        private LogWorker worker;

        /**
         * Creates an instance for the given LogWorker.
         *
         * @param worker The LogWorker that will create the LogObject to log.
         */
        public LogThread(LogWorker worker) {
            super();
            this.worker = worker;
        }

        /**
         * The function that will actually create the string to write to the file, get the file, and append the line
         * to it.
         */
        @Override
        public void run() {
            LogObject logObject = worker.logCreate();
            Date now = new Date();
            String toWrite = exactDateFormat.format(now) + " | " + logObject.getContent();
            if (!writeFile(getObjectFile(createFileName(fileDateFormat.format(now), logObject.getFileName())), toWrite)) {
                // if false is returned, something went wrong somewhere, so warn
                log.error(TAG, "Failed to write log – file operations probably failing.");
            }
        }
    } // end runnable
}
