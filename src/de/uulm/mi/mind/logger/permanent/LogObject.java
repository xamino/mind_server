package de.uulm.mi.mind.logger.permanent;

/**
 * @author Tamino Hartmann
 *         This object servers to allow both the file name and new content to be built by the LogWorker.
 */
public interface LogObject {
    public String getFileName();

    public String getContent();
}
