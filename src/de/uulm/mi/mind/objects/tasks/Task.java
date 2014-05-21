package de.uulm.mi.mind.objects.tasks;

import de.uulm.mi.mind.io.Configuration;
import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.messages.*;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.servlet.FilePath;
import de.uulm.mi.mind.servlet.Servlet;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public abstract class Task<I extends Sendable, O extends Sendable> {

    protected Messenger log;
    protected Configuration configuration;
    protected DatabaseController database;
    protected FilePath filePath;
    protected final String TAG;

    /**
     * Task MUST have the default constructor!
     */
    public Task() {
        log = Messenger.getInstance();
        configuration = Configuration.getInstance();
        database = DatabaseController.getInstance();
        filePath = new FilePath(Servlet.getContext());
        TAG = "Task";
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

    /**
     * Safety catch for messages. Should prevent receiving illegal task messages when the task was legal but no
     * message was written.
     *
     * @param msg The message variable to check for null.
     * @return A message. Guaranteed!
     */
    protected Information nullMessageCatch(Data msg) {
        if (msg == null || !(msg instanceof Information)) {
            log.error(TAG, "NullMessage happened! Shouldn't happen, so fix!");
            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.NULL, "Something went wrong in the task but no message was written!");
        }
        return (Information) msg;
    }

    /**
     * Small method for checking the validity of input strings.
     *
     * @param toCheck The string to check.
     * @return Boolean value whether legal stuff is happening.
     */
    protected boolean safeString(String toCheck) {
        return (toCheck != null && !toCheck.isEmpty());
    }
}
