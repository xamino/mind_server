package database.objects;

import database.Data;

/**
 * @author Tamino Hartmann
 */
public class Message implements Data {
    private String message;

    public Message(String msg) {
        this.message = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
