package logic;

import database.Data;
import database.messages.Error;
import logic.modules.LocationModule;
import logic.modules.UserModule;

/**
 * @author Tamino Hartmann
 */
public class EventModuleManager {

    private static EventModuleManager INSTANCE;
    private final UserModule userModule;
    private final LocationModule locationModule;

    public static EventModuleManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new EventModuleManager();
        return INSTANCE;
    }

    private EventModuleManager() {

        userModule = new UserModule();
        locationModule = new LocationModule();

    }


    public Data handleTask(Task operation, Data task) {

        if (operation instanceof Task.User)
            return userModule.run(operation, task);
        else if(operation instanceof Task.Location ||operation instanceof Task.Area ||operation instanceof Task.WifiMorsel)
        {
            return locationModule.run(operation,task);
        }else{
            return new Error("ModuleNotFound", operation.toString());
        }
    }
}
