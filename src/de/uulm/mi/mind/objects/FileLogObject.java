package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.logger.permanent.LogObject;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * Created by Tamino Hartmann on 7/15/14.
 * Object used to send a log to the server.
 */
public class FileLogObject implements Sendable, LogObject {

    // already in use elsewhere, here for convenience
    public final static String SESSIONFILE = "session", POSITIONFILE = "position", POLLFILE = "polling", IPFILE = "ips";

    private String file;
    private String message;

    public void setFile(String file) {
        this.file = file;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getFileName() {
        return file;
    }

    @Override
    public String getContent() {
        return message;
    }
}
