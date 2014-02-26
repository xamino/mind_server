package database.messages;

import database.Information;

/**
 * @author Tamino Hartmann
 */
public class Message implements Information {
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
