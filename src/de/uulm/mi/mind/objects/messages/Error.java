package de.uulm.mi.mind.objects.messages;

import de.uulm.mi.mind.objects.enums.MsgEnum;

/**
 * @author Tamino Hartmann
 *         Standard error class used to send an error to connected clients.
 */
public class Error extends Information {

    public Error(Type type, String description) {
        super(type, description);
    }

    /**
     * Constructor that adds a default message if available, otherwise the description is left empty.
     *
     * @param type
     */
    public Error(Type type) {
        super(type, defaultMsg(type));
    }

    private static String defaultMsg(Type type) {
        switch (type) {
            case WRONG_OBJECT:
                return "You supplied a wrong object for this task!";
            default:
                return "";
        }
    }

    public enum Type implements MsgEnum {
        /**
         * For mobile client.
         */
        SERVER,
        /**
         * For mobile client.
         */
        CONNECTION,
        WRONG_OBJECT,
        DATABASE,
        ILLEGAL_VALUE,
        TASK,
        CAST,
        SECURITY,
        LOGIN, NULL
    }
}