package de.uulm.mi.mind.objects.messages;

/**
 * @author Tamino Hartmann
 */
public class Success extends Information {

    private String description;
    private Type type;

    private Success() {
    }

    public Success(String description) {
        this.type = Type.OK;
        this.description = description;
    }

    public Success(Type type, String description) {
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Success{" +
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

    public enum Type {
        /**
         * For when everything is really okay.
         */
        OK,
        /**
         * Okay, but note that it is a bit different!
         */
        NOTE
    }
}