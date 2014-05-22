package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Saveable;

import javax.servlet.ServletContextEvent;

/**
 * Created by Cassio on 10.05.2014.
 */
interface DatabaseAccess {
    boolean update(Session session, Saveable data);

    boolean delete(Session session, Saveable data);

    Session open();

    void init(ServletContextEvent event);

    void destroy(ServletContextEvent event);

    boolean create(Session session, Saveable data);

    <E extends Saveable> DataList<E> read(Session session, E data);
}
