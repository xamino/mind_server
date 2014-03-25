package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Area;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.Location;
import de.uulm.mi.mind.objects.User;
import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

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

    @Before
    public void testSetup() {
        // init with some data
        dbc.create(new User("user1@mail.de"));
        dbc.create(new User("user2@mail.de", "User2"));
        dbc.create(new User("admin@mail.de", "Admin", true));
    }

    @Test
    public void create() {
        assertFalse(dbc.create(null));
    }

    @Test
    public void read() {
        DataList data = dbc.read(null);
        assertNotNull("Must return a DataList", data);
        assertFalse("List must contain objects", data.isEmpty());
    }

    @Test
    public void update() {
        assertFalse(dbc.update(null));
    }

    @Test
    public void delete() {
        assertFalse(dbc.delete(null)); //TODO delete all?
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
    public void updateUsers() {
        // Update Values
        dbc.create(new User("gummibär@bärendorf.cu"));
        assertTrue(dbc.update(new User("gummibär@bärendorf.cu", "Gummibär")));
        assertTrue(dbc.update(new User("gummibär@bärendorf.cu", "GroßerGummibär", false)));

        // Test update invalid users
        assertFalse(dbc.update(new User(null)));
        assertFalse(dbc.update(new User("")));
        assertFalse(dbc.update(new User("bösergummibär@mail.de")));
    }

    @Test
    public void deleteUsers() {
        // Delete user
        dbc.create(new User("gummibär@bärendorf.cu"));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu")));
        // try once again to delete him
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu"))); //TODO is still deleted

        // delete using different user but same key
        dbc.create(new User("gummibär@bärendorf.cu"));
        assertTrue(dbc.delete(new User("gummibär@bärendorf.cu", "name")));


        assertTrue("Deletion of all Users failed.", dbc.delete(new User(null)));

        // Test delete invalid users
        assertTrue(dbc.delete(new User(""))); //TODO never existed, deleted is true
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
        String path = new File("").getAbsolutePath() + "/web/WEB-INF/mind_odb.data";
        File f = new File(path);
        f.delete();
    }
}
