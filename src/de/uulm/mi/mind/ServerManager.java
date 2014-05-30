package de.uulm.mi.mind;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.io.DatabaseManager;
import de.uulm.mi.mind.json.JsonWrapper;
import de.uulm.mi.mind.task.TaskManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Class to run server initialization after its start
 */
public class ServerManager implements ServletContextListener {

    private static ServletContext context;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        context = servletContextEvent.getServletContext();
        String filePath = context.getRealPath("/");
        Configuration.getInstance().init(filePath);
        DatabaseManager.getInstance().init(servletContextEvent);
        TaskManager.getInstance();
        JsonWrapper.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        DatabaseManager.getInstance().destroy(servletContextEvent);
    }

    public static ServletContext getContext() {
        return context;
    }
}
