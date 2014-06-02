package de.uulm.mi.mind.task;

import de.uulm.mi.mind.ServerManager;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.tasks.Task;
import de.uulm.mi.mind.objects.Arrival;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.None;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * @author Tamino Hartmann
 */
public class TaskManager {

    private static TaskManager INSTANCE;
    private final String TAG;
    private Messenger log;
    private Set<String> taskNames;
    private HashMap<String, Task> taskObjects;
    private final String FILESEPARATOR;

    private TaskManager() {
        log = Messenger.getInstance();
        TAG = "TaskManager";
        FILESEPARATOR = System.getProperty("file.separator");
        taskNames = new HashSet<>();
        taskObjects = new HashMap<>();
        // now register initial tasks
        loadTasks("de.uulm.mi.mind.logic.tasks.all");
        loadTasks("de.uulm.mi.mind.logic.tasks.admin");
        loadTasks("de.uulm.mi.mind.logic.tasks.multiple");
        loadTasks("de.uulm.mi.mind.logic.tasks.user");
        loadTasks("de.uulm.mi.mind.logic.tasks.sensor");
        log.log(TAG, "Registered " + taskNames.size() + " tasks total.");
        log.log(TAG, "Created.");
    }

    public static TaskManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskManager();
        }
        return INSTANCE;
    }

    /**
     * Method that runs a received task.
     *
     * @param arrival The arrival object to use.
     * @return The answer to send back.
     */
    // todo have input validation in the task abstract class that is called beforehand
    public Sendable run(Arrival arrival) {
        final String TASK = arrival.getTask().toLowerCase();
        // check that task exists
        if (!taskNames.contains(TASK)) {
            log.error(TAG, "Task " + TASK + " not found!");
            return new Error(Error.Type.TASK, "Task " + TASK + " is not available!");
        }
        // get task object
        Task task = taskObjects.get(TASK);
        // make sure that the given object fits what the task wants
        Sendable sendable = prepareTaskObject(task, arrival);
        if (sendable == null) {
            log.error(TAG, "Task " + TASK + " was called with wrong supplied object!");
            return new Error(Error.Type.WRONG_OBJECT, "Wrong object for " + TASK + " supplied!");
        }
        // check security permissions
        Set permissibles = task.getTaskPermission();
        if (permissibles == null || permissibles.isEmpty()) {
            // this is public tasks
            return task.doWork(null, sendable, arrival.isCompact());
        } else {
            // these tasks all require authentication
            Active active = Security.begin(null, arrival.getSessionHash());
            if (active == null || !permissibles.contains(active.getAuthenticated().getClass().getSimpleName())) {
                log.error(TAG, "Illegal task " + TASK + " tried!");
                return new Error(Error.Type.SECURITY, "You do not have permission to use this task!");
            }
            // check for admin tasks
            Sendable answer = null;
            // if the task is an admin task, we need to check especially
            if (task.isAdminTask()) {
                // must be user
                if (!(active.getAuthenticated() instanceof User)) {
                    Security.finish(active);
                    log.error(TAG, "Illegal task " + TASK + " tried! Not user!");
                    return new Error(Error.Type.SECURITY, "You do not have permission to use this task! Not user!");
                }
                if (((User) active.getAuthenticated()).isAdmin()) {
                    answer = task.doWork(active, sendable, arrival.isCompact());
                } else {
                    Security.finish(active);
                    log.error(TAG, "Illegal task " + TASK + " tried! Not admin!");
                    return new Error(Error.Type.SECURITY, "You do not have permission to use this task! Not admin!");
                }
            } else {
                answer = task.doWork(active, sendable, arrival.isCompact());
            }
            Security.finish(active);
            return answer;
        }
    }

    /**
     * Method that checks which type of object a task wants and tries to supply that to the doWork method.
     *
     * @param task    The task for which to check.
     * @param arrival The arrival to use.
     * @return The object to pass to the doWork method.
     */
    private Sendable prepareTaskObject(Task task, Arrival arrival) {
        // check if none is required
        if (task.getInputType().equals(None.class)) {
            return new None();
        }
        // check if arrival.getObject is the object required
        else if (arrival.getObject() != null && task.getInputType().isAssignableFrom(arrival.getObject().getClass())) {
            return arrival.getObject();
        }
        // check whether the arrival object itself is required
        else if (task.getInputType().isAssignableFrom(Arrival.class)) {
            return arrival;
        }
        // if all else fails, return null --> error
        return null;
    }

    /**
     * Method that looks within the tasks package and registers all tasks.
     */
    private void loadTasks(String packageName) {
        // todo allow loading from config path
        List<Class<Task>> tasks = new ArrayList<Class<Task>>();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", FILESEPARATOR));
        if (root == null) {
            log.error(TAG, "Root path is null! Does the directory exist?");
            return;
        }
        // Filter .class files.
        String pathFix = ServerManager.getContext().getRealPath("/")+"WEB-INF/classes/"+packageName.replace(".", "/");
        File[] files = new File(pathFix).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });
        // Find classes implementing Task
        for (File file : files) {
            String className = file.getName().replaceAll(".class$", "");
            Class<?> cls = null;
            try {
                cls = Class.forName(packageName + "." + className);
                if (Task.class.isAssignableFrom(cls)) {
                    tasks.add((Class<Task>) cls);
                }
            } catch (ClassNotFoundException e) {
                log.error(TAG, "Class " + className + " could not be found! Skipping...");
            }
        }
        // Instantiate them and register
        int counter = 0;
        for (Class<Task> task : tasks) {
            Task toAdd = null;
            try {
                toAdd = ((Task) task.getDeclaredConstructors()[0].newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error(TAG, "Failed to get an instance of " + task.getSimpleName() + " via default constructor!");
                continue;
            }
            // check for null
            if (toAdd.getTaskName() == null || toAdd.getTaskName().isEmpty()) {
                log.error(TAG, "Task name for " + toAdd.getClass().getSimpleName() + " is NULL or EMPTY, skipping!");
                continue;
            }
            // check that all API calls are unique
            if (taskNames.contains(toAdd.getTaskName())) {
                log.error(TAG, "Task " + toAdd.getTaskName() + " already exists, skipping "
                        + toAdd.getClass().getSimpleName() + "!");
                continue;
            }
            // otherwise okay, we can register
            final String TASK = toAdd.getTaskName().toLowerCase();
            taskNames.add(TASK);
            taskObjects.put(TASK, toAdd);
            log.log(TAG, "Added task " + TASK + " from " + toAdd.getClass().getSimpleName() + ".");
            counter++;
        }
        log.log(TAG, "Done registering " + counter + " new tasks.");
    }
}
