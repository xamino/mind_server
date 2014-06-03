package de.uulm.mi.mind.tasks.admin;

import de.uulm.mi.mind.io.Session;
import de.uulm.mi.mind.io.Transaction;
import de.uulm.mi.mind.tasks.AdminTask;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.security.Active;
import de.uulm.mi.mind.security.BCrypt;

/**
 * @author Tamino Hartmann
 */
public class SensorAdd extends AdminTask<WifiSensor, Sendable> {
    @Override
    public boolean validateInput(WifiSensor object) {
        return safeString(object.getKey());
    }

    @Override
    public Sendable doWork(Active active, final WifiSensor sensor, boolean compact) {
        // password
        if (safeString(sensor.getTokenHash())) {
            sensor.setTokenHash(BCrypt.hashpw(sensor.getTokenHash(), BCrypt.gensalt(12)));
            return (Sendable) database.open(new Transaction() {
                @Override
                public Data doOperations(Session session) {

                    boolean success = session.create(sensor);
                    if (success) {
                        return new Success("WifiSensor was created successfully.");
                    } else {
                        // some kind of error occurred
                        return new Error(Error.Type.DATABASE, "Creation of WifiSensor resulted in an error.");
                    }
                }
            });
        }
        // otherwise we need to generate a token
        final String token = generateKey();
        // hash it
        sensor.setTokenHash(BCrypt.hashpw(token, BCrypt.gensalt(12)));
        return (Sendable) database.open(new Transaction() {
            @Override
            public Data doOperations(Session session) {
                boolean success = session.create(sensor);
                if (success) {
                    return new Success(Success.Type.NOTE, token);
                } else {
                    // some kind of error occurred
                    return new Error(Error.Type.DATABASE, "Creation of WifiSensor resulted in an error.");
                }
            }
        });
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
