package de.uulm.mi.mind.logic.modules;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * @author Tamino Hartmann
 *         Module for managing WifiSensor user objects.
 */
public class SensorModule implements Module {

    private final String TAG = "SensorModule";
    private final DatabaseController database;
    private Messenger log;

    /**
     * Public constructor.
     */
    public SensorModule() {
        log = Messenger.getInstance();
        database = DatabaseController.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (request == null) {
            request = new WifiSensor(null, null, null);
        } else if (!(request instanceof WifiSensor)) {
            return new Error(Error.Type.WRONG_OBJECT, "Sensor tasks always require a WifiSensor object!");
        }

        WifiSensor sensor = (WifiSensor) request;

        Task.Sensor todo = (Task.Sensor) task;
        switch (todo) {
            case CREATE:
                return createSensor(sensor);
            case READ:
                return readSensor(sensor);
            case UPDATE:
                return updateSensor(sensor);
            case DELETE:
                return deleteSensor(sensor);
            default:
                break;
        }

        return new Error(Error.Type.TASK, "The Sensor Module is unable to perform the Task as it appears not to be implemented.");
    }

    private Data createSensor(WifiSensor sensor) {
        if (sensor.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "WifiSensor to be created was null!");
        }

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

    private Data readSensor(WifiSensor sensor) {
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

    private Data updateSensor(WifiSensor sensor) {
        if (sensor.getKey() == null) {
            return new Error(Error.Type.WRONG_OBJECT, "WifiSensor to be created was null!");
        }

        ObjectContainer sessionContainer = database.getSessionContainer();

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

    private Data deleteSensor(WifiSensor sensor) {
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
}
