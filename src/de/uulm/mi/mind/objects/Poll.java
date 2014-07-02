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
    private int allowedOptionSelections;
    private PollState state;
    private String icon;
    private DataList<PollOption> options = new DataList<>();
    private String unique;

    public Poll() {
    }

    public Poll(String question, Date end) {
        this.question = question;
        this.end = end;
    }

    @Override
    public String toString() {
        return "Poll{" +
                "key='" + key + '\'' +
                ", question='" + question + '\'' +
                ", created=" + created +
                ", end=" + end +
                ", owner='" + owner + '\'' +
                ", allowedOptionSelections=" + allowedOptionSelections +
                ", state=" + state +
                ", icon='" + icon + '\'' +
                ", options=" + options +
                '}';
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

    @Override
    public Saveable deepClone() {
        Poll p = new Poll(question, end);
        p.setAllowedOptionSelections(allowedOptionSelections);
        p.setCreated(created);
        p.setIcon(icon);
        p.setKey(key);
        p.setOwner(owner);
        p.setState(state);
        p.setUnique(unique);

        DataList<PollOption> pollOptions = new DataList<>();
        for (PollOption pollOption : options) {
            if (pollOption == null) continue;
            pollOptions.add((PollOption) pollOption.deepClone());
        }
        p.setOptions(pollOptions);

        return p;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }
}
