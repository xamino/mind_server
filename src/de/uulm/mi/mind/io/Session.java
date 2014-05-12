package de.uulm.mi.mind.io;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;

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


    public boolean create(Data data) {
        return dba.create(this, data);
    }

    public <E extends Data> DataList<E> read(E data) {
        return dba.read(this, data);
    }

    public boolean update(Data data) {
        return dba.create(this, data);
    }

    public boolean delete(Data data) {
        return dba.create(this, data);
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
