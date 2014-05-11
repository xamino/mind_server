package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;

/**
 * Created by Cassio on 10.05.2014.
 */
public interface DatabaseAccess {
    Session open();

    void init();

    void destroy();
}
