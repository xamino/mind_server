package database.objects;

import database.Data;

/**
 * Created by Cassio on 21.02.14.
 */
public class Success implements Data {

    private String message;
    private String description;

    public Success(String message, String description) {
        this.message = message;
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                "message='" + message + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
