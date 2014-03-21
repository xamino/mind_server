package de.uulm.mi.mind.objects.messages;

/**
 * @author Tamino Hartmann
 *         Standard error class used to send an error to connected clients.
 */
public class Error extends Information {


    public Error(String name, String description) {
        super(name, description);
    }

    @Override
    public String toString() {
        return "Error{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}