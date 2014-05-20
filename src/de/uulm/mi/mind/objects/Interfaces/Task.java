package de.uulm.mi.mind.objects.Interfaces;

import de.uulm.mi.mind.security.Authenticated;

import java.util.Collection;

/**
 * @author Tamino Hartmann
 */
public abstract class Task<T extends Sendable> {

    /**
     * Task MUST have the default constructor!
     */
    public Task() {
    }

    /**
     * Method that is called for doing the task.
     *
     * @param object The object requested.
     * @return The object to return.
     */
    public abstract T doWork(T object);

    /**
     * Return the name of the task we want to register. The name given is exactly the API call that is publicly
     * accessible.
     *
     * @return The name.
     */
    public abstract String getTaskName();

    /**
     * Returns the Authenticated objects that may call this task.
     *
     * @return A collection of the Authenticated classes.
     */
    public abstract Collection<Class<? extends Authenticated>> getTaskPermission();
}
