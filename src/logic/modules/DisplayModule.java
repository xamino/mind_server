package logic.modules;

import database.Data;
import database.messages.Error;
import database.objects.PublicDisplay;
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
        if (request instanceof PublicDisplay) {
            Task.Display todo = (Task.Display) task;
            switch (todo) {
                case CREATE:
                    return create((PublicDisplay)request);
                case READ:
                    return read((PublicDisplay)request);
                case UPDATE:
                    return update((PublicDisplay)request);
                case DELETE:
                    return delete((PublicDisplay)request);
                default:
                    break;
            }
        }
        return new Error("UnknownDisplayTask","The requested task could not be executed in DisplayModule!");
    }
}
