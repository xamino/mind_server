package database.objects;

/**
 * @author Tamino Hartmann
 * Standard error class used to send an error to connected clients.
 */
public class Error {
    private String name;
    private String description;

    public Error(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDesc() {
        return description;
    }

    public void setDesc(String desc) {
        this.description = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
