package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.User;
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
    public static void testSetup() {
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
        assertFalse(dbc.create(new User(null)));
        assertFalse(dbc.create(new User("")));
    }

    @Test
    public void createUsersPerformance() {
        for (int i = 0; i < 1000; i++) {
            assertTrue(dbc.create(new User("dummy" + i + "@dummy.du")));
        }
    }

    // Run after all tests here
    @AfterClass
    public static void testCleanup() {
        dbc.deleteAll(new User(null, null));
        dbc.close();
    }
}
