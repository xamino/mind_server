package de.uulm.mi.mind.objects.Interfaces;

import de.uulm.mi.mind.security.Active;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public abstract class Task<I extends Sendable, O extends Sendable> {

    /**
     * Task MUST have the default constructor!
     */
    public Task() {
    }

    /**
     * Method that is called for doing the task. Note that while you are ensured to only receive objects of the type you
     * require, it is still up to the task to check that it is a valid object (so check whether it is null!).
     *
     * @param object The object requested.
     * @return The object to return.
     */
    public abstract O doWork(Active active, I object);

    /**
     * Return the name of the task we want to register. The name given is exactly the API call that is publicly
     * accessible.
     *
     * @return The name.
     */
    public abstract String getTaskName();

    /**
     * Returns the Authenticated objects that may call this task. You can either list the exact objects to specify
     * exactly who may access this; or just use Authenticated to state that any logged in user may use this task; or
     * finally just return null to allow any and all registered and un-registered users to use this task.
     *
     * @return A collection of the Authenticated classes.
     */
    public abstract Set<String> getTaskPermission();

    public abstract Class<I> getInputType();

    public abstract Class<O> getOutputType();

    public abstract boolean isAdminTask();
}
