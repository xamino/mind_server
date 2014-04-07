package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Cassio on 07.04.2014.
 */
public class DatabaseControllerScenariosTest {

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
    public void deleteAll() {
        dbc.delete(null);
        assertTrue(dbc.read(null).isEmpty());
    }


    @Test
    public void updateConnectedUser() {
        User request = new User("user1@mail.de");
        DataList<User> userList = dbc.read(request);
        assertTrue(userList.size() == 1);
        User user = userList.get(0);
        user.setName("NewName");
        dbc.update(user);
        userList = dbc.read(user);
        assertTrue(userList.size() == 1);
        User updatedUser = userList.get(0);
        assertEquals(user.getName(), updatedUser.getName());
    }

    @Test
    public void updateDisconnectedUser() {
        User request = new User("user1@mail.de");
        request.setName("NewName");
        dbc.update(request);
        DataList<User> userList = dbc.read(new User("user1@mail.de"));
        assertTrue("Duplicated user found.", userList.size() == 1);
        User updatedUser = userList.get(0);
        assertEquals(request.getName(), updatedUser.getName());
    }

    @Test
    public void createdUserTwice() {
        dbc.create(new User("ac"));
        assertFalse(dbc.create(new User("ac")));
    }

    @Test
    public void deleteAllUsers() {
        dbc.delete(new User(null));
        assertTrue("All users should have been deleted but there are still some left.", dbc.read(new User(null)).isEmpty());
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
