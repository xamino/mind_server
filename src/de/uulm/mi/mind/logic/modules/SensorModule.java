package de.uulm.mi.mind.logic.modules;

import de.uulm.mi.mind.io.DatabaseController;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.logic.Module;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.Task;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;

/**
 * @author Tamino Hartmann
 *         Module for managing WifiSensor user objects.
 */
public class SensorModule extends Module {

    private final String TAG = "SensorModule";
    private Messenger log;

    /**
     * Public constructor.
     */
    public SensorModule() {
        log = Messenger.getInstance();
    }

    @Override
    public Data run(Task task, Data request) {
        if (request instanceof WifiSensor) {
            Task.Sensor todo = (Task.Sensor) task;
            switch (todo) {
                case CREATE:
                    return create(request);
                case READ:
                    return read(request);
                case UPDATE:
                    return update(request);
                case DELETE:
                    if (((WifiSensor) request).readIdentification() == null) {
                        if (DatabaseController.getInstance().delete(request))
                            return new Success("All WifiSensors were deleted.");
                        else
                            return new de.uulm.mi.mind.objects.messages.Error(Error.Type.DATABASE, "Failed to delete wifi sensors!");
                    }
                    return delete(request);
                default:
                    break;
            }
        }
        return null;
    }
}
