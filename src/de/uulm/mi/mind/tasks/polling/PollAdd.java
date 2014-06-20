package de.uulm.mi.mind.tasks.polling;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Poll;
import de.uulm.mi.mind.objects.PollOption;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.enums.PollState;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.PollTask;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tamino Hartmann on 6/3/14.
 */
public class PollAdd extends PollTask<Poll, Information> {

    /**
     * Default offset of endtime from create time if no end time is given.
     */
    private final long DEFAULT_ENDOFFSET = 30 * 60 * 1000;
    private final int GENERATED_KEY_LENGTH = 8;
    private final int QUESTION_LENGTH = 50;
    private final int ANSWER_LENGTH = 25;

    @Override
    public boolean validateInput(Poll object) {
        // check options
        if (object.getOptions() == null || object.getOptions().isEmpty()) {
            return false;
        }
        for (PollOption option : object.getOptions()) {
            if (!safeString(option.getOptionValue())) {
                return false;
            }
        }
        // we check: question, allowed options must be at least 1
        return safeString(object.getQuestion()) && object.getAllowedOptionSelections() > 0;
    }

    @Override
    public Information doWork(Active active, Poll poll, boolean compact) {
        final Poll toSave;
        // check length of strings
        if (poll.getQuestion().length() > QUESTION_LENGTH) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Question may be max " + QUESTION_LENGTH + " chars long!");
        }
        for (PollOption option : poll.getOptions()) {
            if (option.getOptionValue().length() > ANSWER_LENGTH) {
                return new Error(Error.Type.ILLEGAL_VALUE, "Option answer may be max " + ANSWER_LENGTH + " chars long!");
            }
        }
        // if no end date was sent along we use now + 30min
        if (poll.getEnd() == null) {
            Date end = new Date(System.currentTimeMillis() + DEFAULT_ENDOFFSET);
            toSave = new Poll(poll.getQuestion(), end);
        } else {
            // take provided
            toSave = new Poll(poll.getQuestion(), poll.getEnd());
        }
        // set created if provided, else use now
        if (poll.getCreated() == null) {
            toSave.setCreated(new Date());
        } else {
            toSave.setCreated(poll.getCreated());
        }
        // Set icon if provided, else default
        if (safeString(poll.getIcon())) {
            toSave.setIcon(poll.getIcon());
        } else {
            // todo default icon?
            toSave.setIcon("default");
        }
        // set poll state (ongoing because we created it)
        toSave.setState(PollState.ONGOING);
        // set owner to current user
        toSave.setOwner(((User) active.getAuthenticated()).getEmail());

        // generate key for poll (required for db; we use a random hash)
        // this is done before the options are set because they require this key too
        toSave.setKey(generateKey());

        // prepare and set options
        DataList<PollOption> options = poll.getOptions();
        for (PollOption option : options) {
            // ensure that no users are already set!
            option.setUsers(new ArrayList<String>());
            // set key
            option.setKey(toSave.getKey() + ":" + option.getOptionValue());
        }
        toSave.setOptions(options);
        toSave.setAllowedOptionSelections(poll.getAllowedOptionSelections());

        // save to db
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(toSave);
                if (success) {
                    return new Success("Poll created!");
                } else {
                    return new Error(Error.Type.DATABASE, "Failed to create poll!");
                }
            }
        });
    }

    protected String generateKey() {
        String key = new BigInteger(130, new SecureRandom()).toString(32);
        if (key.length() > GENERATED_KEY_LENGTH) {
            return key.substring(0, GENERATED_KEY_LENGTH);
        }
        return key;
    }

    @Override
    public String getTaskName() {
        return "poll_add";
    }

    @Override
    public Class<Poll> getInputType() {
        return Poll.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
