package de.uulm.mi.mind.io;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Saveable;

/**
 * Created by Cassio on 10.05.2014.
 */
public class Session {

    ObjectContainer getDb4oContainer() {
        return db4oContainer;
    }

    ObjectContainerSQL getSqlContainer() {
        return sqlContainer;
    }

    private final ObjectContainerSQL sqlContainer;
    private final ObjectContainer db4oContainer;
    private final DatabaseAccess dba;

    public Session(ObjectContainer container, DatabaseAccess dba) {
        db4oContainer = container;
        sqlContainer = null;
        this.dba = dba;
    }

    public Session(ObjectContainerSQL container, DatabaseAccess dba) {
        sqlContainer = container;
        db4oContainer = null;
        this.dba = dba;
    }


    public boolean create(Saveable data) {
        return dba.create(this, data);
    }

    public <E extends Saveable> DataList<E> read(E data) {
        return dba.read(this, data, 5);
    }


    /**
     * @param data
     * @param depth Depths of children to be returned.
     *              0 = object fields are initialized to defaults,
     *              1 object fields loaded, 2 children are initialized to defaults,
     *              3 children field loaded etc;
     *              e.g. 5 could be Area-DataList-Location-DataList-WifiMorsel loaded but no further DataList
     * @param <E>
     * @return
     */
    public <E extends Saveable> DataList<E> read(E data, int depth) {
        return dba.read(this, data, depth);
    }

    public boolean update(Saveable data) {
        return dba.update(this, data);
    }

    public boolean delete(Saveable data) {
        return dba.delete(this, data);
    }

    void rollback() {
        if (db4oContainer != null) {
            db4oContainer.rollback();
        } else {
            sqlContainer.rollback();
        }
    }

    void commit() {
        if (db4oContainer != null) {
            db4oContainer.commit();
        } else {
            sqlContainer.commit();
        }
    }

    void close() {
        if (db4oContainer != null) {
            db4oContainer.close();
        } else {
            sqlContainer.close();
        }
    }

    public void reinit() {
        dba.reinit(this);
    }
}
