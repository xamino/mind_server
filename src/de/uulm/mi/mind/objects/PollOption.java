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
    private String unique;

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
    public String toString() {
        return "PollOption{" +
                "key='" + key + '\'' +
                ", optionValue='" + optionValue + '\'' +
                ", users=" + users +
                '}';
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Saveable deepClone() {
        PollOption p = new PollOption(key, optionValue);
        p.setUsers(users); // TODO clone?
        p.setUnique(unique);
        return p;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }
}
