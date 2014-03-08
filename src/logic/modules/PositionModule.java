package logic.modules;

import database.Data;
import database.messages.Message;
import logger.Messenger;
import logic.Module;
import logic.Task;

/**
 * @author Tamino Hartmann
 *         Module that calculates a position based on a given set of WifiMorsels in a Location object.
 */
public class PositionModule extends Module {

    private Messenger log;

    public PositionModule() {
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        return new Message("Position has not been implemented yet!");
    }
}
