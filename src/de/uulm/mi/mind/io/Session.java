package de.uulm.mi.mind.io;

import com.db4o.ObjectContainer;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;

/**
 * Created by Cassio on 10.05.2014.
 */
public abstract class Session {

    private final ObjectContainerSQL sqlContainer;
    private final ObjectContainer db4oContainer;

    public Session(ObjectContainer container) {
        db4oContainer = container;
        sqlContainer = null;
    }

    public Session(ObjectContainerSQL container) {
        sqlContainer = container;
        db4oContainer = null;
    }


    boolean create(Data data){
        if(db4oContainer!=null){
            db4oContainer.s
        }
        else{

        }
    }

    <E extends Data> DataList<E> read(E data);

    boolean update(Data data);

    boolean delete(Data data);

    void rollback();

    void commit();

    void close();
}
