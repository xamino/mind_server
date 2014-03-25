package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Cassio on 07.03.14.
 */
public class DatabaseControllerTest {

    private static DatabaseController dbc;

    // Run before all tests here
    @BeforeClass
    public static void caseSetup() {
        dbc = DatabaseController.getInstance();
        dbc.init(new File("").getAbsolutePath() + "/web/", false);
    }

    @Test
    public void createUsers() {
        // Test creation with all constructors
        assertTrue(dbc.create(new User("gummibär@bärendorf.cu")));
        assertTrue(dbc.create(new User("kuchen@zuckerland.kl", "Honigkuchen")));
        assertTrue(dbc.create(new User("admin@admin.de", "Admin", true)));

        // Test invalid E-Mail Addresses
        assertFalse(dbc.create(null));
        assertFalse(dbc.create(new User(null)));
        assertFalse(dbc.create(new User("")));
    }

    @Test
    public void updateUsers() {
        // Update Values
        assertTrue(dbc.create(new User("gummibär@bärendorf.cu")));
        assertTrue(dbc.update(new User("gummibär@bärendorf.cu", "Gummibär")));
        assertTrue(dbc.update(new User("gummibär@bärendorf.cu", "GroßerGummibär", false)));

        // Test update invalid users
        assertFalse(dbc.update(null));
        assertFalse(dbc.update(new User(null)));
        assertFalse(dbc.update(new User("")));
        assertFalse(dbc.update(new User("bösergummibär@mail.de")));
    }

    @Test
    public void deleteUsers() {
        // Delete user
        assertTrue(dbc.create(new User("gummibär@bärendorf.cu")));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu")));
        // try once again to delete him
        assertFalse(dbc.delete(new User("gummibär@bärendorf.cu")));

        // delete using different user but same key
        assertTrue(dbc.create(new User("gummibär@bärendorf.cu")));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu", "name")));

        // Test delete order
        assertTrue(dbc.create(new User("gummibär1@bärendorf.cu")));
        assertTrue(dbc.create(new User("gummibär2@bärendorf.cu")));
        assertFalse(dbc.delete(new User("gummibär2@bärendorf.cu")));
        assertFalse(dbc.delete(new User("gummibär1@bärendorf.cu")));

        assertTrue(dbc.create(new User("gummibär1@bärendorf.cu")));
        assertTrue(dbc.create(new User("gummibär2@bärendorf.cu")));
        assertFalse(dbc.delete(new User("gummibär1@bärendorf.cu")));
        assertFalse(dbc.delete(new User("gummibär2@bärendorf.cu")));

        // Test delete invalid users
        assertFalse(dbc.delete(null));
        assertFalse(dbc.delete(new User(null)));
        assertFalse(dbc.delete(new User("")));
    }

    @Test
    public void userPerformance() {
        for (int i = 0; i < 1000; i++) {
            assertTrue(dbc.create(new User("dummy" + i + "@dummy.du")));
        }
        for (int i = 0; i < 1000; i++) {
            assertTrue(dbc.update(new User("dummy" + i + "@dummy.du", "dummy" + i)));
        }
        for (int i = 0; i < 1000; i++) {
            assertFalse(dbc.read(new User("dummy" + i + "@dummy.du")).isEmpty());
        }
        for (int i = 0; i < 1000; i++) {
            assertTrue(dbc.delete(new User("dummy" + i + "@dummy.du")));
        }
    }

    @After
    public void testCleanup() {
        System.out.println("---Cleanup---");
        dbc.deleteAll(new User(null));
        dbc.deleteAll(new Area(null));
        dbc.deleteAll(new Location(0, 0));
    }

    // Run after all tests here
    @AfterClass
    public static void caseCleanup() {
        System.out.println("---FinalCleanup---");
        dbc.close();
    }
}
