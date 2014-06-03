package de.uulm.mi.mind.logic.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.logic.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class SensorRemove extends AdminTask<WifiSensor, Information> {
    @Override
    public boolean validateInput(WifiSensor object) {
        return true;
    }

    @Override
    public Information doWork(Active active, final WifiSensor sensor, boolean compact) {
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.delete(sensor);

                if (success) {
                    if (sensor.getKey() == null) {
                        return new Success("All WifiSensors were deleted successfully.");
                    }
                    return new Success("WifiSensor was deleted successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Deletion of WifiSensor resulted in an error.");
                }

            }
        });
    }

    @Override
    public String getTaskName() {
        return "sensor_remove";
    }

    @Override
    public Class<WifiSensor> getInputType() {
        return WifiSensor.class;
    }

    @Override
    public Class<Information> getOutputType() {
        return Information.class;
    }
}
