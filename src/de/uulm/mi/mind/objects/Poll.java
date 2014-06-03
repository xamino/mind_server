package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.PollState;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Tamino Hartmann on 6/3/14.
 * Poll object for storing and working with polls.
 */
public class Poll implements Sendable, Saveable {

    private String key;
    private String question;
    private Date created;
    private Date[] occurrences;
    private PollState state;
    private HashMap<User, Boolean> votes;
    private String icon;

    public Poll() {
        // I hope this never turns up :P
        this.key = "EMPTY";
    }

    public Poll(String question, Date created) {
        this.question = question;
        this.created = created;
        // create unique key
        this.key = created.toString() + ":" + question;
    }

    public Date getCreated() {
        return created;
    }

    public String getQuestion() {
        return question;
    }

    public Date[] getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Date[] occurrences) {
        this.occurrences = occurrences;
    }

    public PollState getState() {
        return state;
    }

    public void setState(PollState state) {
        this.state = state;
    }

    public HashMap<User, Boolean> getVotes() {
        return votes;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
