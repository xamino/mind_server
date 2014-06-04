package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.PollState;

import java.util.Date;

/**
 * Created by Tamino Hartmann on 6/3/14.
 * Poll object for storing and working with polls.
 */
public class Poll implements Sendable, Saveable {

    private String key;
    private String question;
    private Date created;
    private Date end;
    private String owner;
    private int allowedOptionSelections = 1;
    private PollState state;
    private String icon = "default";
    private DataList<PollOption> options = new DataList<>();

    public Poll() {
    }

    public Poll(String question, Date end) {
        this.question = question;
        this.end = end;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public DataList<PollOption> getOptions() {
        return options;
    }

    public void setOptions(DataList<PollOption> options) {
        this.options = options;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public PollState getState() {
        return state;
    }

    public void setState(PollState state) {
        this.state = state;
    }

    public int getAllowedOptionSelections() {
        return allowedOptionSelections;
    }

    public void setAllowedOptionSelections(int allowedOptionSelections) {
        this.allowedOptionSelections = allowedOptionSelections;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getQuestion() {
        return question;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
