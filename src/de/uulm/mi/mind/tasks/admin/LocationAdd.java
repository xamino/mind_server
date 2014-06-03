package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.LocationTask;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class LocationAdd extends LocationTask<Location, Information> {

    @Override
    public boolean validateInput(Location object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, final Location location, boolean compact) {

        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                // If a location already exists we simply update it
                DataList<Location> read = session.read(new Location(location.getCoordinateX(), location.getCoordinateY()));
                if (read.isEmpty()) {
                    // this probably means that no location was found for the given location
                    // so filter
                    Location filteredLocation = filterMorsels(location);
                    // and create
                    boolean success1 = session.create(filteredLocation);
                    // area has changed, so redo mapping
                    boolean success2 = updateMapping(session);

                    if (success1 && success2) {
                        return new Success("Location was created successfully.");
                    }

                    // Evaluate Error
                    if (!success1) {
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error.");
                    } else { //!success2
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error: The mapping could not be updated.");
                    }

                } else {
                    // If a location already exists, we simply add the wifimorsels of the given one
                    Location exist = read.get(0);
                    exist.getWifiMorsels().addAll(location.getWifiMorsels());
                    // filter
                    exist = filterMorsels(exist);
                    boolean success1 = session.update(exist);
                    // area has changed, so redo mapping
                    boolean success2 = updateMapping(session);

                    if (success1 && success2) {
                        return new Success("Location was not created but updated successfully as it existed already.");
                    }

                    // Evaluate Error
                    if (!success1) {
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error.");
                    } else { //!success2
                        return new Error(Error.Type.DATABASE, "Creation of location resulted in an error: The mapping could not be updated.");
                    }
                }
            }
        });
    }

    @Override
    public String getTaskName() {
        return "location_add";
    }

    @Override
    public Class<Location> getInputType() {
        return Location.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
