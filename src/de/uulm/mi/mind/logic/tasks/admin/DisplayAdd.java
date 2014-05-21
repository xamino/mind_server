package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class DisplayAdd extends AdminTask<PublicDisplay, Sendable> {

    private final int GENERATED_KEY_LENGTH = 8;

    @Override
    public Sendable doWork(Active active, PublicDisplay display) {
        // check identification
        if (!safeString(display.getIdentification())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Identification is primary key! May not be empty.");
        }
        // check coordinates
        if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Coordinates must be positive!");
        }
        // password
        if (safeString(display.getToken())) {
            display.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
            ObjectContainer sessionContainer = database.getSessionContainer();
            boolean success = database.create(sessionContainer, display);
            if (success) {
                sessionContainer.commit();
                sessionContainer.close();
                return new Success("PublicDisplay was created successfully.");
            } else {
                // some kind of error occurred
                sessionContainer.rollback();
                sessionContainer.close();
                return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
            }
        }
        // otherwise we need to generate a token
        String token = generateKey();
        // hash it
        display.setToken(BCrypt.hashpw(token, BCrypt.gensalt(12)));
        if (display.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "PublicDisplay to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.create(sessionContainer, display);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            // if successful, return the token
            return new Success(Success.Type.NOTE, token);
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
        }
    }

    private String generateKey() {
        String key = new BigInteger(130, new SecureRandom()).toString(32);
        if (key.length() > GENERATED_KEY_LENGTH) {
            return key.substring(0, GENERATED_KEY_LENGTH);
        }
        return key;
    }

    @Override
    public String getTaskName() {
        return "display_add";
    }

    @Override
    public Class<PublicDisplay> getInputType() {
        return PublicDisplay.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
