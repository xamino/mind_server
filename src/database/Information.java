package database;


/**
 * @author Tamino Hartmann
 *         Interface for simple messages that are to be sent. Used to differentiate these from server data.
 */
public abstract class Information implements Data {
    protected String name;
    protected String description;

    public Information(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
