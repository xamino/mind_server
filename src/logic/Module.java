package logic;

import database.Data;

/**
 * Created by tamino on 2/19/14.
 */
public abstract class Module {
    /**
     * Modules never return null, make sure all methods either return a valid Data object or Success/Error Object.
     *
     * @param task
     * @param request
     * @return The requested Data Object or an Success/Error Message depnending on the Task
     */
    public abstract Data run(Task task, Data request);

}
