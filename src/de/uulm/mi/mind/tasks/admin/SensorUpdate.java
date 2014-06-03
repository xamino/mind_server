package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Information;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class SensorUpdate extends AdminTask<WifiSensor, Information> {
    @Override
    public boolean validateInput(WifiSensor object) {
        return safeString(object.getKey());
    }

    @Override
    public Information doWork(Active active, final WifiSensor sensor, boolean compact) {
        if (!safeString(sensor.readIdentification())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Identification must not be empty!");
        }
        DataList<WifiSensor> read = database.read(new WifiSensor(sensor.readIdentification(), null, null));
        if (read == null) {
            return new Error(Error.Type.DATABASE, "Reading of WifiSensor resulted in an error.");
        }
        // from here on only objects with a valid key == single ones are queried
        else if (read.isEmpty() || read.size() != 1) {
            return new Error(Error.Type.DATABASE, "WifiSensor could not be found!");
        }
        final WifiSensor originalSensor = read.get(0);
        // check password
        if (safeString(sensor.getTokenHash())) {
            originalSensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
        }
        if (safeString(sensor.getArea())) {
            originalSensor.setArea(sensor.getArea());
        }

        //TODO set other fields area etc?

        // update
        return (Information) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {

                boolean success = session.update(originalSensor);
                if (success) {
                    return new Success("WifiSensor was created successfully.");
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Creation of WifiSensor resulted in an error.");
                }
            }
        });
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
