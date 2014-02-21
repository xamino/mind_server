package logic;

import database.Data;
import database.objects.User;
import logic.modules.LocationModule;

import java.util.ArrayList;

/**
 * @author Tamino Hartmann
 */
public class EventModuleManager {

    private static EventModuleManager INSTANCE;

    private ArrayList<Module> moduleList;

    public static EventModuleManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new EventModuleManager();
        return INSTANCE;
    }

    private EventModuleManager(){
        moduleList = new ArrayList<Module>();

        createModules();

    }

    private void createModules() {
        moduleList.add(new LocationModule());
    }


    public Data doWork() {
        return new User("tamino");
    }
}
