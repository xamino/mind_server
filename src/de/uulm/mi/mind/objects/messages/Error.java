package de.uulm.mi.mind.objects.messages;

/**
 * @author Tamino Hartmann
 *         Standard error class used to send an error to connected clients.
 */
public class Error extends Information {

    private String description;
    private Type type;

    private Error() {
    }

    public Error(Type type, String description) {
        this.type = type;

        this.description = description;
    }

    /**
     * Constructor that adds a default message if available, otherwise the description is left empty.
     *
     * @param type
     */
    public Error(Type type) {
        this.type = type;
        this.description = defaultMsg(type);
    }

    @Override
    public String toString() {
        return "Error{" +
                "type=" + type +
                ", description='" + description + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private String defaultMsg(Type type) {
        switch (type) {
            case WRONG_OBJECT:
                return "You supplied a wrong object for this task!";
            default:
                return "";
        }
    }

    public enum Type {
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