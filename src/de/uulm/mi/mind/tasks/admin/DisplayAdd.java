package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class DisplayAdd extends AdminTask<PublicDisplay, Sendable> {

    @Override
    public boolean validateInput(PublicDisplay object) {
        return safeString(object.getKey());
    }

    @Override
    public Sendable doWork(Active active, final PublicDisplay display, boolean compact) {
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
            return (Sendable) database.open(new Transaction() {
                @Override
                public Data doOperations(Session session) {
                    boolean success = session.create(display);

                    if (success) {
                        return new Success("PublicDisplay was created successfully.");
                    } else {
                        return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
                    }
                }
            });
        }

        // otherwise we need to generate a token
        final String token = generateKey();
        // hash it
        display.setToken(BCrypt.hashpw(token, BCrypt.gensalt(12)));
        if (display.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "PublicDisplay to be created was null!");
        }
        return (Sendable) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(display);

                if (success) {
                    return new Success(Success.Type.NOTE, token);
                } else {
                    return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
                }
            }
        });

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
