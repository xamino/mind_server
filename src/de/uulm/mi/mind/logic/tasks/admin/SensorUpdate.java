package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class SensorUpdate extends AdminTask<WifiSensor, Information> {
    @Override
    public Information doWork(Active active, WifiSensor sensor) {
        // todo 2 sessionContainers
        if (!safeString(sensor.readIdentification())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
        }
        ObjectContainer sessionContainer = database.getSessionContainer();
        DataList<WifiSensor> read = database.read(sessionContainer, new WifiSensor(sensor.readIdentification(), null, null));
        sessionContainer.close();
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of WifiSensor resulted in an error.");
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty() || read.size() != 1) {
            return new Error(Error.Type.DATABASE, "WifiSensor could not be found!");
        }
        WifiSensor originalSensor = read.get(0);
        // check password
        if (safeString(sensor.getTokenHash())) {
            originalSensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
        }
        if (safeString(sensor.getArea())) {
            originalSensor.setArea(sensor.getArea());
        }
        // update
        sessionContainer = database.getSessionContainer();

        boolean success = database.update(sessionContainer, sensor);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            return new Success("WifiSensor was created successfully.");
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of WifiSensor resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "sensor_update";
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
