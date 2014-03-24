package de.uulm.mi.mind.logic;

import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.logic.modules.*;

/**
 * @author Tamino Hartmann
 */
public class EventModuleManager {

    private static EventModuleManager INSTANCE;
    private final UserModule userModule;
    private final LocationModule locationModule;
    private final SanitationModule sanitationModule;
    private final PositionModule positionModule;
    private final DisplayModule displayModule;

    private EventModuleManager() {

        // Watch out that no recursive constructors happen! All modules MUST NOT call getInstance() of the moduleManager
        // in their constructors!
        userModule = new UserModule();
        locationModule = new LocationModule();
        sanitationModule = new SanitationModule();
        positionModule = new PositionModule();
        displayModule = new DisplayModule();
    }

    /**
     * Gets an instance of the EventModuleManager.
     *
     * @return
     */
    public static EventModuleManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new EventModuleManager();
        return INSTANCE;
    }

    public Data handleTask(Task operation, Data task) {

        if (operation instanceof Task.User)
            return userModule.run(operation, task);
        else if (operation instanceof Task.Location || operation instanceof Task.Area) {
            return locationModule.run(operation, task);
        } else if (operation instanceof Task.Sanitation) {
            return sanitationModule.run(operation, task);
        } else if (operation instanceof Task.Position) {
            return positionModule.run(operation, task);
        } else if (operation instanceof Task.Display) {
            return displayModule.run(operation, task);
        } else {
            return new Error("ModuleNotFound", operation.toString());
        }
    }
}
