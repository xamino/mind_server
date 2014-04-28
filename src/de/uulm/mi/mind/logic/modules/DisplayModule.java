package de.uulm.mi.mind.logic.modules;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * Module that handles all tasks related to the public displays.
 */
public class DisplayModule implements Module {

    private final String TAG = "DisplayModule";
    private final DatabaseController database;
    private Messenger log;

    /**
     * Public constructor.
     */
    public DisplayModule() {
        log = Messenger.getInstance();
        database = DatabaseController.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (request == null) {
            request = new PublicDisplay(null, null, null, 0, 0);
        } else if (!(request instanceof PublicDisplay)) {
            return new Error(Error.Type.WRONG_OBJECT, "Display tasks always require a PublicDisplay object!");
        }

        PublicDisplay display = (PublicDisplay) request;

        Task.Display todo = (Task.Display) task;
        switch (todo) {
            case CREATE:
                return createDisplay(display);
            case READ:
                return readDisplay(display);
            case UPDATE:
                return updateDisplay(display);
            case DELETE:
                return deleteDisplay(display);
            default:
                break;
        }

        return new Error(Error.Type.TASK, "The Display Module is unable to perform the Task as it appears not to be implemented.");
    }


    private Data createDisplay(PublicDisplay display) {
        if (display.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "PublicDisplay to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.create(sessionContainer, display);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("PublicDisplay was created successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
        }
    }

    private Data readDisplay(PublicDisplay display) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<PublicDisplay> read = database.read(sessionContainer, display);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of PublicDisplay resulted in an error.");
        }

        // get filtered locations
        if (display.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "PublicDisplay could not be found!");
        }
        return read;
    }

    private Data updateDisplay(PublicDisplay display) {
        if (display.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "PublicDisplay to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.update(sessionContainer, display);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("PublicDisplay was created successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
        }

    }

    private Data deleteDisplay(PublicDisplay display) {
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.delete(sessionContainer, display);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            if (display.getKey() == null) {
                return new Success("All PublicDisplay were deleted successfully.");
            }
            return new Success("PublicDisplay was deleted successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Deletion of PublicDisplay resulted in an error.");
        }
    }
}
