package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Created by Cassio on 07.03.14.
 */
@Ignore
public class DatabaseControllerPTest {

    private static DatabaseController dbc;

    // Run before all tests here
    @BeforeClass
    public static void caseSetup() {
        System.out.println("---Test Setup---");
        dbc = DatabaseController.getInstance();
        dbc.init(new File("").getAbsolutePath() + "/web/", false);

        // init 10000 users
        for (int i = 0; i < 10000; i++) {
            dbc.create(new User("readDummy" + i + "@dummy.du"));
            dbc.create(new User("updateDummy" + i + "@dummy.du"));
            dbc.create(new User("deleteDummy" + i + "@dummy.du"));
        }
        System.out.println("---Test Complete---");
    }

    @Test
    public void userCreatePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            dbc.create(new User("createDummy" + i + "@dummy.du"));
        }
        System.out.println("Create: " + (System.currentTimeMillis() - time) + "ms");
    }


    @Test
    public void userUpdatePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            dbc.update(new User("updateDummy" + i + "@dummy.du", "dummy" + i));
        }
        System.out.println("Update: " + (System.currentTimeMillis() - time) + "ms");
    }


    @Test
    public void userReadPerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            dbc.read(new User("readDummy" + i + "@dummy.du")).isEmpty();
        }
        System.out.println("Read: " + (System.currentTimeMillis() - time) + "ms");
    }

    @Test
    public void userDeletePerformance() {
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            dbc.delete(new User("deleteDummy" + i + "@dummy.du"));
        }
        System.out.println("Delete: " + (System.currentTimeMillis() - time) + "ms");
    }

    // Run after all tests here
    @AfterClass
    public static void caseCleanup() {
        System.out.println("---Test Cleanup---");
        dbc.deleteAll(new User(null));
        dbc.deleteAll(new Area(null));
        dbc.deleteAll(new Location(0, 0));
        dbc.close();
        System.out.println("---Cleanup Complete---");
    }
}
