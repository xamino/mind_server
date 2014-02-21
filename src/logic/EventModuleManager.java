package logic;

import database.Data;
import database.objects.User;
import logic.modules.LocationModule;
import logic.modules.UserModule;

import java.util.ArrayList;

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

    private EventModuleManager(){

        userModule = new UserModule();
        locationModule = new LocationModule();

    }


    public Data handleTask(String operation, Data task) {
        return userModule.run(operation, task);
    }
}
