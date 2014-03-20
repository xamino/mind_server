package logic.modules;

import database.Data;
import database.DatabaseController;
import database.messages.Error;
import database.messages.Success;
import database.objects.DataList;
import database.objects.PublicDisplay;
import logger.Messenger;
import logic.Module;
import logic.Task;

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
                    return create((PublicDisplay) request);
                case READ:
                    Data possiblyList = read((PublicDisplay) request);
                    // If single object, return single object:
                    if (possiblyList != null && possiblyList instanceof DataList && ((DataList) possiblyList).size() == 1) {
                        return (Data) ((DataList) possiblyList).get(0);
                    }
                    return possiblyList;
                case UPDATE:
                    return update((PublicDisplay) request);
                case DELETE:
                    if (((PublicDisplay) request).readIdentification() == null) {
                        if (DatabaseController.getInstance().deleteAll(request))
                            return new Success("DisplayAllDeleted", "All displays were deleted.");
                        else
                            return new Error("DisplayAllDeleteFailed", "Failed to delete displays.");
                    }
                    return delete((PublicDisplay) request);
                default:
                    break;
            }
        }
        return null;
    }
}
