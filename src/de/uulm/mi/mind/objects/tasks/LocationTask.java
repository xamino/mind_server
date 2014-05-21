package de.uulm.mi.mind.objects.tasks;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.Interfaces.Sendable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tamino Hartmann on 5/21/14.
 * Implementation of the Task class to allow easy access to methods that many location tasks require.
 */
public abstract class LocationTask extends AdminTask<Location, Sendable> {
    protected ArrayList<String> wifiNameFilter;

    public LocationTask() {
        super();
        wifiNameFilter = configuration.getWifiNameFilter();
    }

    @Override
    public Class<Location> getInputType() {
        return Location.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }

    /**
     * Function that filters the WifiMorsels of a Location against the given list to filter out unwanted wifi hotspots
     * (for example temporary ones or ones we don't want messing with the algorithm).
     *
     * @param location The Location object to filter.
     * @return The location object with the filtered morsels.
     */
    protected Location filterMorsels(Location location) {
        DataList<WifiMorsel> morsels = new DataList<>();
        for (WifiMorsel morsel : location.getWifiMorsels()) {
            for (String s : wifiNameFilter) {
                String[] nameChannel = s.split("\\|");
                String name = nameChannel[0];

                boolean sameChannel = true;
                if (nameChannel.length > 1) {
                    String channel = nameChannel[1];
                    if (!channel.equals("*") && !channel.equals(String.valueOf(morsel.getWifiChannel()))) {
                        sameChannel = false;
                    }
                }
                if (name.equals(morsel.getWifiName()) && sameChannel) {
                    morsels.add(morsel);
                }
            }
        }
        location.setWifiMorsels(morsels);
        return location;
    }

    /**
     * Method that updates the Location <--> Area mapping.
     *
     * @param sessionContainer
     */
    protected boolean updateMapping(ObjectContainer sessionContainer) {
        DataList<Location> locations = database.read(sessionContainer, new Location(0, 0, null));
        DataList<Area> areas = database.read(sessionContainer, new Area(null));

        log.pushTimer(this, "");
        for (Area area : areas) {
            area.setLocations(new DataList<Location>());
            for (Location location : locations) {
                if (area.contains(location.getCoordinateX(), location.getCoordinateY())) {
                    area.addLocation(location);
                }
            }
            // must write data back to DB
            if (!database.update(sessionContainer, area)) {
                log.error(TAG, "Failed to update mapping in DB for " + area.getID() + "!");
                return false;
            }
        }

        log.log(TAG, "Updated Location <--> Area mapping. Took " + log.popTimer(this).time + "ms.");
        return true;
    }
}
