/* TODO:
 - test that *_read with no object returns list of *
 - *_read should always return array
 - *_read should be called multiple times (to prevent stuff like with Location from happening again)
 */


/**
 * Function that runs a unit test. WARNING: test is done with synchronous ajax calls, which means the webpage will be
 * unresponsive until the unit test is done!
 */
function doUnitTest() {

    if (!confirm("WARNING: This will clear the DB! Do you really want to continue?")) {
        return;
    }

    alert("Beginning comprehensive unit test!");

    cleanDB();

    adminRightsTest();
    userUpdateTest();
    userAccessTest();
    areaTest();
    positionTest();
    displayAdminTest();
    displayUserTest();

    cleanDB();

    alert("Comprehensive unit test finished!");
}

function adminRightsTest() {
    alert("Beginning admin rights test.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    // Deactivate admin
    unitTest("toggle_admin", null, Success, adminSession);

    // try admin access
    unitTest("read_all_admin", null, Error, adminSession);
    // Switch to admin rights:
    // TODO this shouldn't work later on!
    unitTest("toggle_admin", null, Success, adminSession);
    // Do an admin task:
    unitTest("admin_user_add", new User("maria.heilig@gott.de", "maria", "Maria Heilig"), Success, adminSession);
    var list = unitTest("read_all_admin", null, Array, adminSession);
    if (list == null || list.length != 1) {
        alert("There should be only one admin in the DB at this point!");
    }
    list = unitTest("admin_user_read", new User(null, null, null), Array, adminSession);
    if (list == null || list.length != 2) {
        alert("There should be only two users in the DB!");
    }
    // admin user management
    unitTest("admin_user_add", new User("email", "password", "name"), Success, adminSession);
    unitTest("admin_user_add", new User("lang@email.de", "password", "Etwas längerer Name, mit Sonderzeichen und so!"), Success, adminSession);
    unitTest("admin_user_add", new User("", "", null), Error, adminSession);
    unitTest("admin_user_add", new User("", "legal", null), Error, adminSession);
    // this should return a key
    unitTest("admin_user_add", new User("legal", "", null), Message, adminSession);
    // update tests
    unitTest("admin_user_update", new User("email", null, "name name"), Success, adminSession);
    unitTest("admin_user_update", new User("lang@email.de", null, null, true), Success, adminSession);
    // test password remains untouched (message because first login):
    unitTest("login", new User("email", "password", null), Message, null);
    unitTest("admin_user_update", new User("email", "new password", null), Success, adminSession);
    unitTest("login", new User("email", "new password", null), Success, null);
    // check if really admin
    var langSession = unitTest("login", new User("lang@email.de", "password"), Message, null).description;
    unitTest("admin_user_read", new User(null, null, null, true), Array, langSession);
    // remove
    unitTest("admin_user_delete", new User("email", null, null), Success, adminSession);
    // test that there is only one other user left
    var list = unitTest("admin_user_read", new User(null, null, null), Array, adminSession);
    if (list != null && list.length != 4) {
        // 4 because Maria is still there and legal :P
        alert("Too many few users! Should be only 4 here.")
    }
    // Test user update
    unitTest("user_update", new User("admin@admin.admin", null, "administrator"), Success, adminSession);
    // logout
    unitTest("logout", null, Success, adminSession);
    // login
    unitTest("login", new User("admin@admin.admin", "admin", null), Success, null);

    cleanDB();

    alert("Admin rights test done.");
}

function userUpdateTest() {
    alert("Beginning user update test.");

    unitTest("registration", new User("maria.heilig@gott.de", "maria", "Maria Heilig"), Success, null);
    var mariaSession = unitTest("login", new User("maria.heilig@gott.de", "maria", null), Success, null).description;
    // Test user update
    unitTest("user_update", new User("maria.heilig@gott.de", "jesus", "Maria Joseph"), Success, mariaSession);
    var readMaria = unitTest("user_read", null, User, mariaSession);
    if (!("maria.heilig@gott.de" === readMaria.email && "Maria Joseph" === readMaria.name)) {
        alert("User update failed!");
    }
    // logout && login
    unitTest("logout", null, Success, mariaSession);
    // Old login should fail
    unitTest("login", new User("maria.heilig@gott.de", "maria", null), Error, null);
    // new one should work
    mariaSession = unitTest("login", new User("maria.heilig@gott.de", "jesus", null), Success, null).description;
    // changing email shouldn't work
    unitTest("user_update", new User("maria.schein.heilig@gott.de", "adam", "Peter Peter"), Error, mariaSession);

    cleanDB();

    alert("User update test done.");
}

function userAccessTest() {
    alert("Beginning registration, login, logout.");
    // Check initial registration
    unitTest("registration", new User("maria.heilig@gott.de", "maria", "Maria Heilig"), Success, null);
    // Create some more users for testing purposes
    unitTest("registration", new User("ego.trump@haha.com", "ßüöä", "Ego Trump"), Success, null);
    // try registering again
    unitTest("registration", new User("admin@admin.admin", "admin", null), Error, null);
    // try illegal registration
    unitTest("registration", new User("", "admin", ""), Error, null);
    // try login
    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    var mariaSession = unitTest("login", new User("maria.heilig@gott.de", "maria", null), Success, null).description;
    var egoSession = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null).description;
    // double session test
    var egoTwoSession = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null).description;
    // illegal login
    unitTest("login", new User("mister.x@world.org", "bomb", null), Error, null);
    // try logout
    unitTest("logout", null, Success, adminSession);
    // illegal logout
    unitTest("logout", null, Success, "not_a_hash");
    // Re-login the admin
    adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    // Check
    unitTest("check", null, Success, egoSession);
    unitTest("check", null, Success, egoTwoSession);
    // double session stuff
    unitTest("logout", null, Success, egoTwoSession);
    unitTest("check", null, Error, egoSession);
    egoSession = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null).description;
    // check that all users are in db
    var list = unitTest("admin_user_read", new User(null, null, null), Array, adminSession);
    if (list == null || list.length != 3) {
        alert("Wrong number of users read from server!");
    }

    // delete
    unitTest("user_delete", null, Success, adminSession);
    unitTest("user_delete", null, Success, mariaSession);
    unitTest("user_delete", null, Success, egoSession);
    // Illegal delete
    unitTest("user_delete", null, Error, "illegal_hash");

    cleanDB();

    alert("Registration, login, logout done.");
}

function areaTest() {
    alert("Beginning area, location.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;

    var wifis1 = [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -93),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -90),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -85)
    ];
    var wifis2 = [
        new WifiMorsel("00:19:07:00:65:00", "eduroam", -54),
        new WifiMorsel("00:19:07:00:65:01", "welcome", -34),
        new WifiMorsel("00:19:07:00:66:02", "welcome", -12)
    ];
    // test adding a new area
    unitTest("area_add", new Area("Office Prof. Gott", null, 34, 56, 3, 4), Success, adminSession);
    // adding locations via area should result in error
    unitTest("area_add", new Area("Office Prof. Gott", [new Location(34, 57, wifis1)], 34, 56, 3, 4), Error, adminSession);
    // test universe area
    unitTest("location_add", new Location(1, 1, wifis2), Success, adminSession);
    var area = unitTest("area_read", new Area("universe", null, 0, 0, 0, 0), Array, adminSession);
    if (!instanceOf(area[0], Area)) {
        alert("Reading universe failed - not Area type object!");
    } else {
        var location = area[0].locations[0];
        if (location == null && !((location.coordinateX == 1) && (location.coordinateY == 1) )) {
            alert("Writing a location directly to the universal area failed!");
        }
    }
    // Test adding a location to multiple ares
    unitTest("location_add", new Location(35, 58, wifis1), Success, adminSession);
    var universe = unitTest("area_read", new Area("universe", null, 0, 0, 0, 0), Array, adminSession);
    var office = unitTest("area_read", new Area("Office Prof. Gott", null, 0, 0, 0, 0), Array, adminSession);
    // should be in both
    var test = false;
    var locations = universe[0].locations;
    for (var i = 0; i < locations.length; i++) {
        if (locations[i].coordinateX == 35 && locations[i].coordinateY == 58)
            test = true;
    }
    if (!test) {
        alert("Multiple area location failed! Not in universe.")
    }
    locations = office[0].locations;
    for (var i = 0; i < locations.length; i++) {
        if (locations[i].coordinateX == 35 && locations[i].coordinateY == 58)
            test = true;
    }
    if (!test) {
        alert("Multiple area location failed! Not in office.")
    }
    // test adding a new area without locations, should contain corresponding locations after the fact!
    unitTest("location_add", new Location(111, 111, wifis1), Success, adminSession);
    unitTest("area_add", new Area("Toilet", null, 100, 100, 20, 20), Success, adminSession);
    var toilet = unitTest("area_read", new Area("Toilet", null, 0, 0, 0, 0), Array, adminSession);
    toilet = toilet[0];
    if (toilet.locations == undefined || toilet.locations.length != 1) {
        alert("New area was not correctly populated with previous locations!");
    }

    cleanDB();

    alert("Area, Location done.");
}

function positionTest() {
    alert("Beginning position.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;

    var location1 = new Location(100, 100, [
        new WifiMorsel("00:19:07:06:64:00", "eduroam", -93),
        new WifiMorsel("00:19:07:06:64:01", "test", -90),
        new WifiMorsel("00:19:07:06:64:02", "welcome", -85)
    ]);
    var location2 = new Location(200, 200, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -80),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -70),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -60)
    ]);
    var location3 = new Location(150, 150, [
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -100),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -50),
        new WifiMorsel("A0:19:07:07:64:02", "eduroam", -30)
    ]);

    var locationRequest1 = new Location(0, 0, [
        new WifiMorsel("00:19:07:06:64:00", "eduroam", -92),
        new WifiMorsel("00:19:07:06:64:01", "test", -91),
        new WifiMorsel("00:19:07:06:64:02", "welcome", -84)
    ]);
    // note switched order of wifimorsels
    var locationRequest2 = new Location(42,42, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -80),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -60),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -70)
    ]);
    var locationRequest3 = new Location(1, 1, [
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -98),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -49),
        new WifiMorsel("A0:19:07:07:64:02", "eduroam", -32)
    ]);

    unitTest("location_add", location1, Success, adminSession);
    unitTest("location_add", location2, Success, adminSession);
    unitTest("location_add", location3, Success, adminSession);
    // add area that coverse location3
    unitTest("area_add", new Area("office", null, 145, 145, 10, 10), Success, adminSession);
    // area that covers loc3 + 2
    unitTest("area_add", new Area("institute", null,140, 140, 100, 100), Success, adminSession);

    // Test exact location match to universe (location1)
    var match = unitTest("position_find", locationRequest1, Area, adminSession);
    if (instanceOf(match, Area)) {
        if (match.ID != "universe") {
            alert("Failed 'find_position'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one.");
        }
    }
    // Test close location match
    match = unitTest("position_find", locationRequest2, Area, adminSession);
    if (instanceOf(match, Area)) {
        if (match.ID != "institute") {
            alert("Failed 'find_position'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one.");
        }
    }
    // test match to office
    match = unitTest("position_find", locationRequest3, Area, adminSession);
    if (instanceOf(match, Area)) {
        if (match.ID != "office") {
            alert("Failed 'find_position'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one.");
        }
    }
    // TODO more, especially check for errors!

    cleanDB();

    alert("Position done.");
}

/**
 * Test all functionality for the public displays.
 */
function displayAdminTest() {

    alert("Beginning display admin test.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    // register some displays:
    unitTest("display_add", new PublicDisplay("office_herman", "herman_token", "Office Prof. Herman", 56, 78), Success, adminSession);
    unitTest("display_add", new PublicDisplay("instituts_sek", "sekretariat", "Sekretariat", 33, 23), Success, adminSession);
    // should fail
    unitTest("display_add", new PublicDisplay("instituts_sek", "___", "___", 343, 234), Error, adminSession);
    // key test
    unitTest("display_add", new PublicDisplay("terra", null, "Earth", 3435, 34534), Message, adminSession);
    unitTest("display_remove", new PublicDisplay("terra", null, null, 0, 0), Success, adminSession);
    // should not be allowed as normal user
    unitTest("registration", new User("ego.trump@haha.com", "ßüöä", "Ego Trump"), Success, null);
    var egoSession = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null).description;
    unitTest("display_add", new PublicDisplay("office_ego", "das hier erratet ihr nie!", "MEINS", 156, 178), Error, egoSession);
    // test
    var displays = unitTest("display_read", new PublicDisplay(null, null, null, 0, 0), Array, adminSession);
    if (displays != null && displays.length != 2) {
        alert("Wrong number of displays! Should be 2 here.");
    }
    // update
    unitTest("display_update", new PublicDisplay("office_doof", "herman_token", "Office Prof. Herman", 56, 78), Error, adminSession);
    unitTest("display_update", new PublicDisplay("office_herman", null, "Office Herman", 56, 78), Success, adminSession);
    var test = unitTest("display_read", new PublicDisplay("office_herman", null, null, 0, 0), Array, adminSession);
    if (test.length != 1 || test[0].location != "Office Herman") {
        alert("Update failed!");
    }
    // legal remove:
    unitTest("display_remove", new PublicDisplay("instituts_sek", null, null, null, null), Success, adminSession);
    // illegal remove:
    unitTest("display_remove", new PublicDisplay("i don't exist!", null, null, null, null), Success, adminSession);
    // test, should be only one remaining now
    test = unitTest("display_read", new PublicDisplay(null, null, null, null, null), Array, adminSession);
    if (test.length != 1) {
        alert("Too many few displays exist!");
    }

    cleanDB();
    alert("Display admin test done.")
}

function displayUserTest() {
    alert("Beginning display user test.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    // register some displays:
    unitTest("display_add", new PublicDisplay("office_herman", "herman_token", "Office Prof. Herman", 56, 78), Success, adminSession);
    unitTest("display_add", new PublicDisplay("instituts_sek", "sekretariat", "Sekretariat", 33, 23), Success, adminSession);

    // login as displays
    var dispOne = unitTest("login", new PublicDisplay("office_herman", "herman_token"), Success, null).description;
    // try user task
    unitTest("position_find", new Location(), Error, dispOne);
    // try admin task
    unitTest("admin_user_read", new User(null, null, null), Error, dispOne);
    unitTest("logout", null, Success, dispOne);

    cleanDB();
    alert("Display user test done.");
}

/**
 * Use this method to clean the DB.
 */
function cleanDB() {
    unitTest("registration", new User("special@admin.eu", "admin", ""), Success, null);
    var received = unitTest("login", new User("special@admin.eu", "admin", null), Success, null);
    if (received == null) {
        received = unitTest("login", new User("special@admin.eu", "admin", null), Message, null);
        if (received == null) {
            alert("Failed clean; could not get valid admin session!");
            return;
        }
    }
    var adminSession = received.description;
    // Check if we are currently an admin... :P
    var admin = JSON.parse($.ajax({
        data: JSON.stringify(new Arrival("user_read", adminSession)),
        async: false
    }).responseText).object;
    if (!admin.admin) {
        unitTest("toggle_admin", null, Success, adminSession);
    }
    // Destroy areas
    unitTest("admin_annihilate_area", null, Success, adminSession);
    var arealist = unitTest("area_read", new Area(), Array, adminSession);
    if (arealist == null || arealist.length != 1 || arealist[0].ID != "universe") {
        alert("DB was NOT CLEARED of AREAS!");
    }
    // Destroy displays
    unitTest("display_remove", new PublicDisplay(), Success, adminSession);
    var displayList = unitTest("display_read", new PublicDisplay(), Array, adminSession);
    if (displayList == null || displayList.length != 0) {
        alert("DB was NOT CLEARED of PUBLIC DISPLAYS!");
    }

    // Destroy users
    unitTest("ADMIN_ANNIHILATE_USER", null, Success, adminSession);
    // Now check with default admin, as admin above was now deleted!
    var newSession = unitTest("login", new User("admin@admin.admin", "admin", null), Message, null).description;
    var list = unitTest("read_all_admin", null, Array, newSession);
    if (list == null || list.length > 1) {
        alert("DB was NOT CLEARED of USERS!");
    } else {
        // i shouldn't exist anymore
        unitTest("check", null, Error, adminSession);
    }
    unitTest("logout", null, Success, newSession);
}

/**
 * Runs a single test.
 * @param task The server API task to execute.
 * @param object_in The object to send.
 * @param object_out The comparison to use for the reply.
 * @param session If applicable, the session to attach – else just use null.
 * @return The object received from the server if the unit test was successful, else null.
 */
function unitTest(task, object_in, object_out, session) {
    var arrival;
    if (object_in != null) {
        arrival = new Arrival(task, session, object_in);
    }
    else {
        arrival = new Arrival(task, session);
    }
    // Synchronously send:
    var response = JSON.parse($.ajax({
        data: JSON.stringify(arrival),
        async: false
    }).responseText).object;

    // Check if we were handed just the class or an object
    if (object_out.$type == undefined) {
        // If just a class, create object
        object_out = new object_out();
    }
    if (!(response.$type === object_out.$type)) {
        alert("Failed '" + task + "'. Expected type '" + object_out.constructor.name + "'.\n\n" + JSON.stringify(response));
        return null;
    } else {
        return response;
    }
}
