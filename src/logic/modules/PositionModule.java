package logic.modules;

import database.Data;
import database.messages.*;
import database.messages.Error;
import database.objects.Location;
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
        if (!(task instanceof Task.Position)) {
            return new Error("WrongTaskType", "PositionModule was called with the wrong task type!");
        }
        if (!(request instanceof Location)) {
            return new Error("WrongObjectType", "PositionModule was called with the wrong object type!");
        }
        // Everything okay from here on out:
        // todo put algorithm here
        return new Message("PositionUnimplemented","Position has not been implemented yet!");
    }
}
