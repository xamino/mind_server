package de.uulm.mi.mind.tasks.user;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.Task;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class MapEmailName extends Task<DataList, Sendable> {

    private String TAG = "MapEmailName";

    @Override
    public boolean validateInput(DataList object) {
        return object != null && !object.isEmpty();
    }

    @Override
    public Sendable doWork(Active active, DataList users, boolean compact) {
        DataList<User> retUsers = new DataList<>();
        // for each sent user with set email
        for (Object object : users) {
            if (!(object instanceof User) || ((User) object).getEmail().isEmpty()) {
                return new Error(Error.Type.ILLEGAL_VALUE, "List may only contain users with emails!");
            }
            User fill = ((User) object);
            // try to get from DB
            DataList<User> read = database.read(new User(fill.getEmail()));
            if (read == null || read.size() != 1) {
                // this means we can't map, so we just copy the email
                retUsers.add(new User(fill.getEmail(), fill.getEmail()));
                continue;
            }
            // this means we have a mapping we can return
            retUsers.add(new User(fill.getEmail(), read.get(0).getName()));
        }
        return retUsers;
    }

    @Override
    public String getTaskName() {
        return "map_email_name";
    }

    @Override
    public Set<String> getTaskPermission() {
        Set<String> permissible = new HashSet<>();
        permissible.add(User.class.getSimpleName());
        return permissible;
    }

    @Override
    public Class<DataList> getInputType() {
        return DataList.class;
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
