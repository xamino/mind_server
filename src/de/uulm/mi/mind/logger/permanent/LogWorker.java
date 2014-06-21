package de.uulm.mi.mind.logger.permanent;

/**
 * @author Tamino Hartmann
 * Interface for implementing a method for creating a LogObject to write the object to a file.
 */

public interface LogWorker {
    public LogObject logCreate();
}
