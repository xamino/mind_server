package de.uulm.mi.mind.objects.messages;


import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.enums.MsgEnum;

/**
 * @author Tamino Hartmann
 *         Interface for simple messages that are to be sent. Used to differentiate these from server data.
 */
public abstract class Information implements Data {
    protected String description;
    protected MsgEnum type;

    protected Information(MsgEnum type, String description) {

        this.description = description;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Information{" +
                "type=" + type +
                ", description='" + description + '\'' +
                '}';
    }

    public MsgEnum getType() {
        return type;
    }

    public void setType(MsgEnum type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getKey() {
        return null;
    }
}
