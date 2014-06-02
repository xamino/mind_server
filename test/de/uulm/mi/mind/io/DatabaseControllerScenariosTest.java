package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.User;
import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Cassio on 07.04.2014.
 */

@Ignore
public class DatabaseControllerScenariosTest {
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
    }

    @After
    public void afterEachTest() {
        session.delete(null);
        session.close();
    }

    @Before
    public void testSetup() {
        // init with some data

    }

    @Test
    public void deleteAll() {
        session.delete(null);
        assertTrue(session.read(null).isEmpty());
    }

    @Test
    public void updateConnectedUser() {
        User request = new User("user1@mail.de");
        DataList<User> userList = session.read(request);
        assertTrue(userList.size() == 1);
        User user = userList.get(0);
        user.setName("NewName");
        session.update(user);
        userList = session.read(user);
        assertTrue(userList.size() == 1);
        User updatedUser = userList.get(0);
        assertEquals(user.getName(), updatedUser.getName());
    }

    @Test
    public void updateDisconnectedUser() {
        User request = new User("user1@mail.de");
        request.setName("NewName");
        session.update(request);
        DataList<User> userList = session.read(new User("user1@mail.de"));
        assertTrue("Duplicated user found.", userList.size() == 1);
        User updatedUser = userList.get(0);
        assertEquals(request.getName(), updatedUser.getName());
    }

    @Test
    public void createdUserTwice() {
        session.create(new User("ac"));
        assertFalse(session.create(new User("ac")));
    }

    @Test
    public void deleteAllUsers() {
        session.delete(new User(null));
        assertTrue("All users should have been deleted but there are still some left.", session.read(new User(null)).isEmpty());
    }

}
