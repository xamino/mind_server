package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class SensorRemove extends AdminTask<WifiSensor, Information> {
    @Override
    public Information doWork(Active active, WifiSensor sensor) {
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.delete(sessionContainer, sensor);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            if (sensor.getKey() == null) {
                return new Success("All WifiSensors were deleted successfully.");
            }
            return new Success("WifiSensor was deleted successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Deletion of WifiSensor resulted in an error.");
        }
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
