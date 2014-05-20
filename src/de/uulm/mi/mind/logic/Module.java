package de.uulm.mi.mind.logic;

import de.uulm.mi.mind.objects.Interfaces.Data;
import de.uulm.mi.mind.objects.enums.Task;

/**
 * @author Andreas Köll, Tamino Hartmann
 */
public interface Module {

    /**
     * Modules never return null, make sure all methods either return a valid Data object or Success/Error Object.
     *
     * @param task
     * @param request
     * @return The requested Data Object or an Success/Error Message depending on the Task
     */
    public Data run(Task task, Data request);

}
