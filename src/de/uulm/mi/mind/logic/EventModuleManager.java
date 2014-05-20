package de.uulm.mi.mind.logic;

import de.uulm.mi.mind.logic.modules.*;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;

/**
 * @author Tamino Hartmann
 */
public class EventModuleManager {

    private static EventModuleManager INSTANCE;
    private final UserModule userModule;
    private final LocationModule locationModule;
    private final PositionModule positionModule;
    private final DisplayModule displayModule;
    private final SensorModule sensorModule;

    private EventModuleManager() {

        // Watch out that no recursive constructors happen! All modules MUST NOT call getInstance() of the moduleManager
        // in their constructors!
        userModule = new UserModule();
        locationModule = new LocationModule();
        positionModule = new PositionModule();
        displayModule = new DisplayModule();
        sensorModule = new SensorModule();
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
        } else if (operation instanceof Task.Position) {
            return positionModule.run(operation, task);
        } else if (operation instanceof Task.Display) {
            return displayModule.run(operation, task);
        } else if (operation instanceof Task.Sensor) {
            return sensorModule.run(operation, task);
        } else {
            return new Error(Error.Type.NULL, operation.toString() + " found no matching module! Has it been registered?");
        }
    }
}
