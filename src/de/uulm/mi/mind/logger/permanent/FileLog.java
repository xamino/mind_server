package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Interfaces.Data;

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
    private final String PATHSEPARATOR;
    private final String LOGSEPARATOR = " | ";
    private final String OBJECTSFOLDER = "objects";
    private final String LOGFOLDER = "mind_log";
    private final String LOGFILETYPE = ".log";
    private final SimpleDateFormat fileDateFormat;
    private final SimpleDateFormat exactDateFormat;
    private boolean LOG;
    private boolean ASYNC = true;
    private String PATH;
    private Messenger log;

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
        // set valid path
        if (PATH == null || PATH.isEmpty()) {
            PATH = System.getProperty("user.home");
            PATH += PATHSEPARATOR + LOGFOLDER;
        }
        // create if nonexistent
        File directory = new File(PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            if (!directory.mkdir()) {
                log.error(TAG, "Failed to create dir at " + PATH);
            }
        }
        log.log(TAG, "Path: " + PATH + " Logging: " + LOG);
        log.log(TAG, "Created.");
    }

    public synchronized static FileLog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileLog();
        }
        return INSTANCE;
    }

    private boolean writeFile(File file, String line) {
        boolean allOkay = true;
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

    private File getObjectFile(String fileName) {
        // create the file name for today, eg "2014_06_22-Poll.log"
        File objectFolder = new File(PATH + PATHSEPARATOR + OBJECTSFOLDER);
        File objectFile = new File(PATH + PATHSEPARATOR + OBJECTSFOLDER + PATHSEPARATOR + fileName);
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

    private String createFileName(String date, String name) {
        return date + "-" + name + LOGFILETYPE;
    }

    public void log(LogWorker worker) {
        LogThread execute = new LogThread(worker);
        if (ASYNC) {
            new Thread(execute).start();
        } else {
            execute.run();
        }
    }

    private class LogThread<E extends Data> implements Runnable {
        private LogWorker worker;

        public LogThread(LogWorker worker) {
            super();
            this.worker = worker;
        }

        @Override
        public void run() {
            LogObject logObject = worker.logCreate();
            Date now = new Date();
            String toWrite = exactDateFormat.format(now) + LOGSEPARATOR + logObject.getContent();
            writeFile(getObjectFile(createFileName(fileDateFormat.format(now), logObject.getFileName())), toWrite);
        }
    }
}
