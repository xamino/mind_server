package database.messages;

import database.Information;

/**
 * Created by Cassio on 21.02.14.
 */
public class Success implements Information {

    private String name;
    private String description;

    public Success(String name, String description) {
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

    @Override
    public String toString() {
        return "Success{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
