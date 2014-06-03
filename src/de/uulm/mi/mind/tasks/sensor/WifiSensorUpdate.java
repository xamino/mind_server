package de.uulm.mi.mind.tasks.sensor;

import de.uulm.mi.mind.tasks.Task;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.security.Active;

import java.util.Set;

/**
 * @author Tamino Hartmann
 */
public class WifiSensorUpdate extends Task<DataList, Information> {
    @Override
    public boolean validateInput(DataList object) {
        return true;
    }

    @Override
    public Information doWork(Active active, DataList devices, boolean compact) {
        // before below code need to check security and validity!
//        for (SensedDevice device : receivedDevices) {
//            if (!sniffedDevices.contains(device.getIpAddress())) {
//                // not sensed before, so just add it and continue
//                log.log(TAG, "Found new device to track: " + device.getIpAddress());
//                sniffedDevices.add(device.getIpAddress(), device);
//                continue;
//            }
//            // already in list, so check if to update if level is higher
//            // todo: we should also check for update of location with a threshold
//            // so that lvl40 but new room is taken over lvl41 old room
//            int oldLevel = sniffedDevices.get(device.getIpAddress()).getLevelValue();
//            if (oldLevel <= device.getLevelValue()) {
//                log.log(TAG, "Updated location of " + device.getIpAddress() + " to " + device.getSensor() + ".");
//                sniffedDevices.add(device.getIpAddress(), device);
//            }
//        }
//        return new Success("Updated lists.");
        return new Error(Error.Type.SERVER, "Task not implemented as new task type! Dummy task.");
    }

    @Override
    public String getTaskName() {
        return "wifi_sensor_update";
    }

    @Override
    public Set<String> getTaskPermission() {
        return null;
    }

    @Override
    public Class<DataList> getInputType() {
        return DataList.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }

    @Override
    public boolean isAdminTask() {
        return false;
    }
}
