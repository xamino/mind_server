package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * Module that handles all tasks related to the public displays.
 */
public class DisplayModule extends Module {

    private final String TAG = "DisplayModule";
    private Messenger log;

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
                    return create(request);
                case READ:
                    return read(request);
                case UPDATE:
                    return update(request);
                case DELETE:
                    if (((PublicDisplay) request).readIdentification() == null) {
                        if (DatabaseController.getInstance().delete(request))
                            return new Success("All displays were deleted.");
                        else
                            return new Error(Error.Type.DATABASE, "Failed to delete displays.");
                    }
                    return delete(request);
                default:
                    break;
            }
        }
        return null;
    }
}
