package de.uulm.mi.mind.logger.permanent;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.logger.Messenger;

/**
 * @author Tamino Hartmann
 */
public class FileLog {
    private static FileLog INSTANCE;
    private final String TAG = "FileLog";
    private final boolean LOG;
    private final String PATH;
    private Messenger log;
    private Configuration configuration;

    private FileLog() {
        // get dependent objects
        log = Messenger.getInstance();
        configuration = Configuration.getInstance();
        // get configs
        LOG = configuration.isFileLogActive();
        PATH = configuration.getFileLogPath();
        log.log(TAG, "Created.");
    }

    public synchronized static FileLog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileLog();
        }
        return INSTANCE;
    }

    public void log() {
        if (!LOG) {
            return;
        }
    }
}
