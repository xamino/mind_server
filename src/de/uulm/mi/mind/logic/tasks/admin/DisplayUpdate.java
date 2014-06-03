package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.AdminTask;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.PublicDisplay;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class DisplayUpdate extends AdminTask<PublicDisplay, Information> {
    @Override
    public boolean validateInput(PublicDisplay object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, final PublicDisplay display, boolean compact) {
        if (!safeString(display.getIdentification())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
        }
        DataList<PublicDisplay> read = database.read(new PublicDisplay(display.getIdentification(), null, null, 0, 0));
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of PublicDisplay resulted in an error.");
        }

        // from here on only objects with a valid key == single ones are queried && make sure we get only 1 back
        if (read.isEmpty() || read.size() != 1) {
            return new Error(Error.Type.DATABASE, "PublicDisplay could not be found!");
        }
        PublicDisplay originalDisplay = read.get(0);
        // check coordinates
        if (display.getCoordinateX() < 0 || display.getCoordinateY() < 0) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Coordinates must be positive!");
        }
        // check password
        if (safeString(display.getToken())) {
            originalDisplay.setToken(BCrypt.hashpw(display.getToken(), BCrypt.gensalt(12)));
        }
        // check location
        if (safeString(display.getLocation())) {
            originalDisplay.setLocation(display.getLocation());
        }
        // update
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session dba) {
                boolean success = dba.update(display);

                if (success) {
                    return new Success("PublicDisplay was created successfully.");
                } else {
                    return new Error(Error.Type.DATABASE, "Creation of PublicDisplay resulted in an error.");
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "display_update";
    }

    @Override
    public Class<PublicDisplay> getInputType() {
        return PublicDisplay.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
