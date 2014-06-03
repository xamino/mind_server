package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
public class PollOption implements Saveable, Sendable {

    private String key;
    private String optionValue;
    private DataList<User> users = new DataList<>();

    private PollOption() {

    }

    public PollOption(String key, String optionValue) {
        this.key = key;
        this.optionValue = optionValue;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public DataList<User> getUsers() {
        return users;
    }

    public void setUsers(DataList<User> users) {
        this.users = users;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
