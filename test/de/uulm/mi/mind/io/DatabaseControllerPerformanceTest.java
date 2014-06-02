package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.*;
import org.junit.*;

import java.io.File;
import java.util.Date;

/**
 * Created by Cassio on 07.03.14.
 */
public class DatabaseControllerPerformanceTest {

    private static DatabaseAccess dbc;
    private Session session;

    // Run before all tests here
    @BeforeClass
    public static void caseSetup() {
        System.out.println("---Test Setup---");
        Configuration.getInstance().init(new File("").getAbsolutePath() + "/web/");
        if (Configuration.getInstance().getDbType().toLowerCase().equals("sql")) {
            dbc = DatabaseControllerSQL.getInstance();
        } else {
            dbc = DatabaseController.getInstance();
        }

        dbc.init(new File("").getAbsolutePath() + "/web/");

        Session session = dbc.open();
        session.delete(null);
        // init 10000 users
        /*for (int i = 0; i < 10000; i++) {
            session.create(new User("readDummy" + i + "@dummy.du"));
            // dbc.create(new User("updateDummy" + i + "@dummy.du"));
            //dbc.create(new User("deleteDummy" + i + "@dummy.du"));
        }*/
        session.commit();
        session.close();
        System.out.println("---Test Setup Complete---");
    }

    // Run after all tests here
    @AfterClass
    public static void caseCleanup() {
        System.out.println("---Test Cleanup---");
        dbc.destroy();
        String path = new File("").getAbsolutePath() + "/web/WEB-INF/" + Configuration.getInstance().getDbName();
        File f = new File(path);
        f.delete();
        System.out.println("---Test cleanup Complete---");
    }

    @Before
    public void beforeEachTest() {
        session = dbc.open();
    }

    @After
    public void afterEachTest() {
        session.delete(null);
        session.close();
    }

    @Ignore
    @Test
    public void userCreatePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            session.create(new User("createDummy" + i + "@dummy.du"));
        }
        System.out.println("Create: " + (System.currentTimeMillis() - time) + "ms");
    }

    @Ignore
    @Test
    public void userUpdatePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            session.update(new User("updateDummy" + i + "@dummy.du", "dummy" + i, false));
        }
        System.out.println("Update: " + (System.currentTimeMillis() - time) + "ms");
    }


    @Test
    public void userReadPerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            session.read(new User("readDummy" + i + "@dummy.du"));
        }
        System.out.println("Read: " + (System.currentTimeMillis() - time) + "ms");
    }

    @Test
    public void userReadAllPerformance() {
        Long time = System.currentTimeMillis();
        DataList<User> userDataList = session.read(new User(null));
        System.out.println("ReadAll: " + userDataList.size() + " - " + (System.currentTimeMillis() - time) + "ms");
    }

    @Ignore
    @Test
    public void userDeletePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            session.delete(new User("deleteDummy" + i + "@dummy.du"));
        }
        System.out.println("Delete: " + (System.currentTimeMillis() - time) + "ms");
    }


    @Test
    public void areaLocationsMorselMadness() {
        DataList<Location> locations = new DataList<>();
        for (int i = 1; i < 31; i++) {
            DataList<WifiMorsel> morsels = new DataList<>();
            for (int j = 1; j < 501; j++) {
                morsels.add(new WifiMorsel("macAddress" + j, "Welcome", -j, 6, "Debugger", new Date()));
            }
            locations.add(new Location(i, i, morsels));
        }
        Area uni = new Area("University", locations, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Long time = System.currentTimeMillis();
        session.create(uni);
        session.commit();
        System.out.println("Write University: " + (System.currentTimeMillis() - time) + "ms");
        time = System.currentTimeMillis();
        DataList<Area> areas = session.read(new Area("University"));
        System.out.println("Read University: " + (System.currentTimeMillis() - time) + "ms");
        time = System.currentTimeMillis();
        DataList<WifiMorsel> morselDataList = session.read(new WifiMorsel(null, null, 0, 0, null, null));
        System.out.println("ReadAll Morsels: " + morselDataList.size() + " - " + (System.currentTimeMillis() - time) + "ms");
    }

}
