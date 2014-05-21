package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;

/**
 * Created by Tamino Hartmann on 5/21/14.
 */
public class SensorRead extends AdminTask<WifiSensor, Sendable> {
    @Override
    public Sendable doWork(Active active, WifiSensor sensor) {
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<WifiSensor> read = database.read(sessionContainer, sensor);
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of WifiSensor resulted in an error.");
        }

        // get filtered locations
        if (sensor.getKey() == null) {
            return read;
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty()) {
            return new Error(Error.Type.DATABASE, "WifiSensor could not be found!");
        }
        return read;
    }

    @Override
    public String getTaskName() {
        return "sensor_read";
    }

    @Override
    public Class<WifiSensor> getInputType() {
        return WifiSensor.class;
    }

    @Override
    public Class<Sendable> getOutputType() {
        return Sendable.class;
    }
}
