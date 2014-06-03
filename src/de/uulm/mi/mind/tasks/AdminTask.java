package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 * <p/>
 * Abstract class for Admin tasks. Implements the User.class with admin as true for the Task class.
 */
public abstract class AdminTask<I extends Sendable, O extends Sendable> extends Task<I, O> {

    private final int GENERATED_KEY_LENGTH = 8;

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public boolean isAdminTask() {
        return true;
    }

    protected String generateKey() {
        String key = new BigInteger(130, new SecureRandom()).toString(32);
        if (key.length() > GENERATED_KEY_LENGTH) {
            return key.substring(0, GENERATED_KEY_LENGTH);
        }
        return key;
    }
}
