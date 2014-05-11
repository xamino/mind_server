package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;

/**
 * Created by Cassio on 11.05.2014.
 */
public interface Transaction {
    Data doOperations(Session session);
}
