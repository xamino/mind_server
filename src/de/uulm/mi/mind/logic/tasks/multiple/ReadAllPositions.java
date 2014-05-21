package de.uulm.mi.mind.logic.tasks.multiple;

import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.tasks.Task;
import de.uulm.mi.mind.objects.enums.Status;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.Security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class ReadAllPositions extends Task<None, Sendable> {

    private final String REAL_POSITION = "realPosition";

    @Override
    public Sendable doWork(Active unused, None object) {
        // read all users
        ArrayList<Active> users = Security.readActives();
        // filter the list – apply status and set special cases
        DataList<User> sendUsers = new DataList<>();
        for (Active active : users) {
            // check if user
            if (!(active.getAuthenticated() instanceof User)) {
                continue;
            }
            User us = ((User) active.getAuthenticated());
            Area area = ((Area) active.readData(REAL_POSITION));
            String position = (area == null ? null : area.getID());
            Status status = us.getStatus();

            // Handle logic as to what should be output
            boolean isAtUniversity = position != null && position.equals("University"); //TODO set in find
            boolean isAtLocation = position != null && !position.equals("University");
            boolean isInvisible = us.getStatus() == null || us.getStatus() == Status.INVISIBLE;

            // Continue means is not added to output
            // remove invisible users
            if (isInvisible) {
                //log.log(TAG,"Is invisible: " + us.getEmail());
                continue;
            }

            // user is somewhere at an unknown location at university
            if (isAtUniversity) {
                position = null; // "University" is not seen from outside but displayed as "away"
                status = Status.AWAY;
            } else if (isAtLocation) {
                // nothing to do?
            } else {
                //log.log(TAG,"Position null: " + us.getEmail());
                continue; // position was null
            }

            // Filter user object to only give name + position
            User toSend = new User(us.getEmail());
            toSend.setName(us.getName());
            toSend.setPosition(position);
            toSend.setStatus(status);
            sendUsers.add(toSend);
        }
        return sendUsers;
    }

    @Override
    public String getTaskName() {
        return "read_all_positions";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(PublicDisplay.class.getSimpleName());
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<None> getInputType() {
        return None.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
