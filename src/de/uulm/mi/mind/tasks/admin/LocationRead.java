package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.WifiMorsel;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.tasks.LocationTask;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class LocationRead extends LocationTask<Location, Sendable> {
    @Override
    public boolean validateInput(Location object) {
        return true;
    }

    @Override
    public Sendable doWork(Active active, Location location, boolean compact) {
        String deviceModelFilter = "";
        if (location.getWifiMorsels() != null && location.getWifiMorsels().size() > 0) {
            deviceModelFilter = location.getWifiMorsels().get(0).getDeviceModel();
        }
        location.setWifiMorsels(null); // reset to not filter these
        DataList<Location> read = database.read(location,5); // TODO extra db method for this call
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of location resulted in an error.");
        }

        // only return wifimorsels of the requesting device model
        if (compact) {
            for (Location location1 : read) {
                DataList<WifiMorsel> filteredMorsels = null;
                if (location1.getWifiMorsels() == null) {
                    continue;
                } else {
                    for (WifiMorsel wifiMorsel : location1.getWifiMorsels()) {
                        // morsel class from device is NOT same as the one in db
                        if (wifiMorsel.getDeviceModel() == null || wifiMorsel.getDeviceModel().equals(deviceModelFilter)) {
                            if (filteredMorsels == null) {
                                filteredMorsels = new DataList<>();
                            }
                            filteredMorsels.add(wifiMorsel);
                        }
                    }
                }
                location1.setWifiMorsels(filteredMorsels);
            }
        }

        // get filtered locations
        if (location.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "Location could not be found!");
        }
        return read;
    }

    @Override
    public String getTaskName() {
        return "location_read";
    }

    @Override
    public Class<Location> getInputType() {
        return Location.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
