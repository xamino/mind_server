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
        session.create(new User("user1@mail.de"));
        session.create(new User("user2@mail.de", "User2", false));
        session.create(new User("admin@mail.de", "Admin", true));
        session.commit();
    }

    @After
    public void afterEachTest() {
        session.delete(null);
        session.commit();
        session.close();
    }

    @Test
    public void createOp() {
        assertFalse(session.create(null));
    }

    @Test
    public void readOp() {
        DataList data = session.read(null);
        assertNotNull("Must return a DataList", data);
        assertFalse("List must contain objects", data.isEmpty());
    }

    @Test
    public void updateOp() {
        assertFalse(session.update(null));
    }

    @Test
    public void deleteOp() {
        assertTrue(session.delete(null));
        assertTrue(session.read(null).isEmpty());
    }

    @Test
    public void createUsersOp() {
        // Test creation with all constructors
        assertTrue(session.create(new User("gummibär@bärendorf.cu")));
        assertTrue(session.create(new User("kuchen@zuckerland.kl", "Honigkuchen", false)));
        assertTrue(session.create(new User("admin@admin.de", "Admin", true)));

        // Test invalid E-Mail Addresses
        assertFalse(session.create(new User(null)));
        assertFalse(session.create(new User("")));
    }

    @Test
    public void updateUsersOp() {
        // Update Values
        assertTrue(session.update(new User("admin@mail.de", "Gummibär", false)));
        assertTrue(session.update(new User("admin@mail.de", "GroßerGummibär", false)));

        // Test update invalid users
        assertFalse(session.update(new User(null)));
        assertFalse(session.update(new User("")));
        assertFalse(session.update(new User("bösergummibär@mail.de")));
    }

    @Test
    public void deleteUsersOp() {
        // Delete user
        session.create(new User("gummibär@bärendorf.cu"));
        assertTrue(session.delete(new User("gummibär@bärendorf.cu")));
        // try once again to delete him, should not work
        assertFalse(session.delete(new User("gummibär@bärendorf.cu")));

        // delete using different user but same key
        session.create(new User("gummibär@bärendorf.cu"));
        assertTrue(session.delete(new User("gummibär@bärendorf.cu", "name", false)));


        assertTrue(session.delete(new User(null)));

        // Test delete invalid, non existing user
        assertFalse("Non existing user was deleted.", session.delete(new User("")));
    }

}
