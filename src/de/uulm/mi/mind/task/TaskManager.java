package de.uulm.mi.mind.task;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Interfaces.Task;
import de.uulm.mi.mind.objects.messages.Error;

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

    private TaskManager() {
        log = Messenger.getInstance();
        taskNames = new HashSet<>();
        taskObjects = new HashMap<>();
        TAG = "TaskManager";
        // now register initial tasks
        loadTasks();
        log.log(TAG, "Created.");
    }

    public static TaskManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskManager();
        }
        return INSTANCE;
    }

    public Sendable run(final String TASK, Sendable object) {
        if (!taskNames.contains(TASK)) {
            log.error(TAG, "Task " + TASK + " not found!");
            return new Error(Error.Type.TASK, "Task " + TASK + " is not available!");
        }
        // todo check authentication
        log.log(TAG, "Running task " + TASK + "...");
        return taskObjects.get(TASK).doWork(object);
    }

    private void loadTasks() {
        // todo allow loading from config path
        // Get our package name
        String packageName = "de.uulm.mi.mind.logic.tasks";
        List<Class<Task>> tasks = new ArrayList<Class<Task>>();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "/"));
        if (root == null) {
            log.error(TAG, "Root path is null! Does the directory exist?");
            return;
        }
        // Filter .class files.
        File[] files = new File(root.getFile()).listFiles(new FilenameFilter() {
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
        for (Class<Task> task : tasks) {
            Task toAdd = null;
            try {
                toAdd = ((Task) task.getDeclaredConstructors()[0].newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error(TAG, "Failed to get an instance of " + task.getSimpleName() + " via default constructor!");
                continue;
            }
            // check that all API calls are unique
            if (taskNames.contains(toAdd.getTaskName())) {
                log.error(TAG, "Task " + toAdd.getTaskName() + " already exists, skipping "
                        + toAdd.getClass().getSimpleName() + "!");
                continue;
            }
            // otherwise okay, we can register
            // todo register authenticated
            taskNames.add(toAdd.getTaskName());
            taskObjects.put(toAdd.getTaskName(), toAdd);
            log.log(TAG, "Added task " + toAdd.getTaskName() + " from " + toAdd.getClass().getSimpleName() + ".");
        }
        log.log(TAG, "Done registering " + taskNames.size() + " tasks.");
    }
}
