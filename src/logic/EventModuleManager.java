package logic;

import database.objects.User;

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
        initModules();

    }

    private void createModules() {
        moduleList.add(new LocationModule());
    }

    private void initModules() {
        for(Module m : moduleList ){
            m.init(this);
        }
    }

    public Object doWork() {
        return new User("tamino");
    }
}
