package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

import java.util.ArrayList;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
public class PollOption implements Saveable, Sendable {

    private String key;
    private String optionValue;
    private ArrayList<String> users = new ArrayList<>();

    private PollOption() {

    }

    public PollOption(String key, String optionValue) {
        this.key = key;
        this.optionValue = optionValue;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
