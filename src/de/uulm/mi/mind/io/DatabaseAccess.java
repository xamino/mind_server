package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;

import javax.servlet.ServletContextEvent;

/**
 * Created by Cassio on 10.05.2014.
 */
public interface DatabaseAccess {
    boolean update(Session session, Data data);

    boolean delete(Session session, Data data);

    Session open();

    void init(ServletContextEvent event);

    void destroy(ServletContextEvent event);

    boolean create(Session session, Data data);

    <E extends Data> DataList<E> read(Session session, E data);

    void reinit(Session session);
}
