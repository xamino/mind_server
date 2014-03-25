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
        assertTrue(dbc.create(new User("Gummibär", "gummibär@bärendorf.cu")));
        assertTrue(dbc.create(new User("Honigkuchen", "kuchen@zuckerland.kl")));
        assertTrue(dbc.create(new User("LilaKuh", "kuh@milka.de")));
        assertTrue(dbc.create(new User("Batman", "bat@ma.n")));
        assertTrue(dbc.create(new User("Strolch", "hunde@ausdemzwing.er")));

        assertFalse(dbc.create(new User(null, null)));
        assertFalse(dbc.create(new User(null, "")));
        assertFalse(dbc.create(new User("", null)));
        assertFalse(dbc.create(new User("", "")));
        assertFalse(dbc.create(new User("abc", null)));
        assertFalse(dbc.create(new User("abc", "")));
    }

    @Test
    public void createUsersPerformance() {
        for (int i = 0; i < 1000; i++) {
            assertTrue(dbc.create(new User("Dummy" + i, "dummy" + i + "@dummy.du")));
        }
    }

    // Run after all tests here
    @AfterClass
    public static void testCleanup() {
        dbc.deleteAll(new User(null, null));
        dbc.close();
    }
}
