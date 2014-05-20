package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Interfaces.Data;

/**
 * A Transaction is the interface between a module and a the access to a database.
 */
public interface Transaction {
    /**
     * Opens a new session on the database. Returning an Error here, rollbacks any attempted operations with this session.
     *
     * @param session a new atomic session on the database
     * @return data or information objects obtained from the database
     */
    Data doOperations(Session session);
}
