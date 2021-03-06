package de.uulm.mi.mind.tasks;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.WifiMorsel;

import java.util.ArrayList;

/**
 * Created by Tamino Hartmann on 5/21/14.
 * Implementation of the Task class to allow easy access to methods that many location tasks require.
 */
public abstract class LocationTask<I extends Sendable, O extends Sendable> extends AdminTask<I, O> {
    protected ArrayList<String> wifiNameFilter;

    public LocationTask() {
        super();
        wifiNameFilter = configuration.getWifiNameFilter();
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
}
