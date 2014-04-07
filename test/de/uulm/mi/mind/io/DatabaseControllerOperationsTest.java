package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

/**
 * This class runs test cases on database operations.
 * Not the object modifications are tested, but the correct execution of operations only.
 */
public class DatabaseControllerOperationsTest {

    private static DatabaseController dbc;

    // Run before all tests here
    @BeforeClass
    public static void caseSetup() {
        dbc = DatabaseController.getInstance();
        dbc.init(new File("").getAbsolutePath() + "/web/", false);
    }

    @Before
    public void testSetup() {
        // init with some data
        dbc.create(new User("user1@mail.de"));
        dbc.create(new User("user2@mail.de", "User2"));
        dbc.create(new User("admin@mail.de", "Admin", true));
    }

    @Test
    public void createOp() {
        assertFalse(dbc.create(null));
    }

    @Test
    public void readOp() {
        DataList data = dbc.read(null);
        assertNotNull("Must return a DataList", data);
        assertFalse("List must contain objects", data.isEmpty());
    }

    @Test
    public void updateOp() {
        assertFalse(dbc.update(null));
    }

    @Test
    public void deleteOp() {
        assertTrue(dbc.delete(null));
        assertTrue(dbc.read(null).isEmpty());
    }

    @Test
    public void createUsersOp() {
        // Test creation with all constructors
        assertTrue(dbc.create(new User("gummibär@bärendorf.cu")));
        assertTrue(dbc.create(new User("kuchen@zuckerland.kl", "Honigkuchen")));
        assertTrue(dbc.create(new User("admin@admin.de", "Admin", true)));

        // Test invalid E-Mail Addresses
        assertFalse(dbc.create(new User(null)));
        assertFalse(dbc.create(new User("")));
    }

    @Test
    public void updateUsersOp() {
        // Update Values
        assertTrue(dbc.update(new User("admin@mail.de", "Gummibär")));
        assertTrue(dbc.update(new User("admin@mail.de", "GroßerGummibär", false)));

        // Test update invalid users
        assertFalse(dbc.update(new User(null)));
        assertFalse(dbc.update(new User("")));
        assertFalse(dbc.update(new User("bösergummibär@mail.de")));
    }

    @Test
    public void deleteUsersOp() {
        // Delete user
        dbc.create(new User("gummibär@bärendorf.cu"));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu")));
        // try once again to delete him, should not work
        assertFalse(dbc.delete(new User("gummibär@bärendorf.cu")));

        // delete using different user but same key
        dbc.create(new User("gummibär@bärendorf.cu"));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu", "name")));


        assertTrue(dbc.delete(new User(null)));

        // Test delete invalid, non existing user
        assertFalse("Non existing user was deleted.", dbc.delete(new User("")));
    }

    @After
    public void testCleanup() {
        System.out.println("---Cleanup---");
        dbc.delete(null);
    }

    // Run after all tests here
    @AfterClass
    public static void caseCleanup() {
        System.out.println("---FinalCleanup---");
        dbc.close();
        String path = new File("").getAbsolutePath() + "/web/WEB-INF/mind_odb.data";
        File f = new File(path);
        f.delete();
    }
}
