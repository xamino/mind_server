package logic;

import java.util.ArrayList;

/**
 * Created by tamino on 2/19/14.
 */
public class EventModuleManager {

    private ArrayList<Module> moduleList;

    EventModuleManager(){
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

}
