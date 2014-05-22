package de.uulm.mi.mind.logic.tasks.admin;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.objects.tasks.AdminTask;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class SensorAdd extends AdminTask<WifiSensor, Sendable> {
    @Override
    public Sendable doWork(Active active, WifiSensor sensor) {
        // check identification
        if (!safeString(sensor.readIdentification())) {
            return new Error(Error.Type.ILLEGAL_VALUE, "Identification is primary key! May not be empty.");
        }
        // password
        if (safeString(sensor.getTokenHash())) {
            sensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
            ObjectContainer sessionContainer = database.getSessionContainer();

            boolean success = database.create(sessionContainer, sensor);

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
        // otherwise we need to generate a token
        String token = generateKey();
        // hash it
        sensor.setTokenHash(BCrypt.hashpw(token, BCrypt.gensalt(12)));
        ObjectContainer sessionContainer = database.getSessionContainer();

        boolean success = database.create(sessionContainer, sensor);

        if (success) {
            sessionContainer.commit();
            sessionContainer.close();
            // If the operation was a success, we need to send the generated key back
            return new Success(Success.Type.NOTE, token);
        } else {
            // some kind of error occurred
            sessionContainer.rollback();
            sessionContainer.close();
            return new Error(Error.Type.DATABASE, "Creation of WifiSensor resulted in an error.");
        }
    }

    @Override
    public String getTaskName() {
        return "sensor_add";
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
