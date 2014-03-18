package logic.modules;

import database.Data;
import logger.Messenger;
import logic.Module;
import logic.Task;

/**
 * Module that handles all tasks related to the public displays.
 */
public class DisplayModule extends Module {

    private Messenger log;
    private final String TAG = "DisplayModule";

    /**
     * Public constructor.
     */
    public DisplayModule() {
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        log.log(TAG, "Task came by!");
        return null;
    }
}
