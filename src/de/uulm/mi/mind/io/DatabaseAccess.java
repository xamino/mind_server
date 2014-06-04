package de.uulm.mi.mind.io;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.User;
import de.uulm.mi.mind.security.BCrypt;

/**
 * Created by Cassio on 10.05.2014.
 */
abstract class DatabaseAccess {
    private static final String TAG = "DatabaseAccess";
    protected Messenger log;

    protected DatabaseAccess() {
        log = Messenger.getInstance();
    }

    abstract boolean update(Session session, Saveable data);

    abstract boolean delete(Session session, Saveable data);

    abstract Session open();

    abstract void init(String contextPath);

    abstract void destroy();

    abstract boolean create(Session session, Saveable data);

    abstract <E extends Saveable> DataList<E> read(Session session, E data);

    void reinit(Session session) {
        final Configuration config = Configuration.getInstance();
        log.log(TAG, "Running DB init.");
        DataList<Area> areaData = session.read(new Area("University"));
        if (areaData == null || areaData.isEmpty()) {
            log.log(TAG, "Universe not existing, creating it.");
            session.create(new Area("University", new DataList<Location>(), 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        // Create default admin if no other admin exists
        User adminProto = new User(null);
        adminProto.setAdmin(true);
        DataList<User> adminData = session.read(adminProto);
        // test for existing single admin or list of admins
        if (adminData == null || adminData.isEmpty()) {
            log.log(TAG, "Admin not existing, creating one.");
            adminProto = new User(config.getAdminEmail(), config.getAdminName(), true);
            adminProto.setPwdHash(BCrypt.hashpw(config.getAdminPassword(), BCrypt.gensalt(12)));
            session.create(adminProto);
        }
    }

    public abstract Session openReadOnly();
}
