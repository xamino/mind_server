/* TODO:
 - test that *_read with no object returns list of *
 - *_read should always return array
 - *_read should be called multiple times (to prevent stuff like with Location from happening again)
 */

/**
 * Function that runs a unit test. NOTE: test is done with synchronous ajax calls, which means the webpage will be
 * unresponsive until the unit test is done!
 */
function doUnitTest() {

    if (!confirm("NOTE: This will clear the DB! Do you really want to continue?")) {
        return;
    }

    alert("Beginning comprehensive unit test!");

    cleanDB();

    adminRightsTest();
    userUpdateTest();
    userAccessTest();
    areaTest();
    positionTest();
    testPositionRead();
    displayAdminTest();
    displayUserTest();
    wifiSensorAPITest();
    statusTest();

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
    var testKey = unitTest("admin_user_add", new User("legal", "", null), Success, adminSession);
    if (testKey.type != "NOTE") {
        alert("Adding a user without password should return a key!\n\n" + JSON.stringify(testKey));
    }
    // update tests
    unitTest("admin_user_update", new User("email", null, "name name"), Success, adminSession);
    unitTest("admin_user_update", new User("lang@email.de", null, null, true), Success, adminSession);
    // test password remains untouched
    unitTest("login", new User("email", "password", null), Success, null);
    unitTest("admin_user_update", new User("email", "new password", null), Success, adminSession);
    unitTest("login", new User("email", "new password", null), Success, null);
    // check if really admin
    var langSession = unitTest("login", new User("lang@email.de", "password"), Success, null).description;
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

    var adminSession = getAdminSession();
    // Check initial registration
    unitTest("registration", new User("ego.trump@haha.com", "ßüöä", "Ego Trump"), Success, null);
    // also create one via admin to test first login
    unitTest("admin_user_add", new User("maria.heilig@gott.de", "maria", "Maria Heilig"), Success, adminSession);
    // try registering again
    unitTest("registration", new User("admin@admin.admin", "admin", null), Error, null);
    // try illegal registration
    unitTest("registration", new User("", "admin", ""), Error, null);
    // try login
    var egoSession = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null).description;
    // login with notification that it is the first login!
    var testType = unitTest("login", new User("maria.heilig@gott.de", "maria", null), Success, null);
    if (testType.type != "NOTE") {
        alert("Failed to notice first login!");
    }
    var mariaSession = testType.description;
    // double session test
    testType = unitTest("login", new User("ego.trump@haha.com", "ßüöä", null), Success, null);
    if (testType.type != "OK") {
        alert("Not first login returns NOTE, should be just OK!");
    }
    var egoTwoSession = testType.description;
    // illegal login
    unitTest("login", new User("mister.x@world.org", "bomb", null), Error, null);
    // try logout
    unitTest("logout", null, Success, adminSession);
    // illegal logout
    unitTest("logout", null, Success, "not_a_hash");
    // Re-login the admin
    adminSession = getAdminSession();
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

    // try login with wrong type
    unitTest("login", new WifiSensor("admin@admin.admin", "admin"), Error, null);

    cleanDB();

    alert("Registration, login, logout done.");
}

function areaTest() {
    alert("Beginning area, location.");

    var adminSession = getAdminSession();

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
    var wifiUpdate = [
        new WifiMorsel("00:01:02:03:04:05", "mind", -45)
    ];
    // test adding a new area
    unitTest("area_add", new Area("Office Prof. Gott", null, 34, 56, 3, 4), Success, adminSession);
    // adding locations via area should result in error
    unitTest("area_add", new Area("Office Prof. Gott", [new Location(34, 57, wifis1)], 34, 56, 3, 4), Error, adminSession);
    // test University area
    unitTest("location_add", new Location(1, 1, wifis2), Success, adminSession);
    var area = unitTest("area_read", new Area("University", null, 0, 0, 0, 0), Array, adminSession);
    if (!instanceOf(area[0], Area)) {
        alert("Reading University failed - not Area type object!");
    } else {
        var location = area[0].locations[0];
        if (location == null && !((location.coordinateX == 1) && (location.coordinateY == 1) )) {
            alert("Writing a location directly to the universal area failed!");
        }
    }
    // Test adding a location to multiple ares
    unitTest("location_add", new Location(35, 58, wifis1), Success, adminSession);
    var university = unitTest("area_read", new Area("University", null, 0, 0, 0, 0), Array, adminSession);
    var office = unitTest("area_read", new Area("Office Prof. Gott", null, 0, 0, 0, 0), Array, adminSession);
    // should be in both
    var test = false;
    var locations = university[0].locations;
    for (var i = 0; i < locations.length; i++) {
        if (locations[i].coordinateX == 35 && locations[i].coordinateY == 58)
            test = true;
    }
    if (!test) {
        alert("Multiple area location failed! Not in University.")
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
    // test double location_add
    unitTest("location_add", new Location(111, 111, wifiUpdate), Success, adminSession);
    var locs = unitTest("location_read", new Location(111, 111, null), Array, adminSession);
    if (locs.length != 1 || locs[0].wifiMorsels.length != 4) {
        alert("Failed to update new Wifimorsels to existing location via location_add!\n\n" + JSON.stringify(locs));
    }

    cleanDB();

    alert("Area, Location done.");
}

function positionTest() {
    alert("Beginning position.");

    var adminSession = getAdminSession();

    // begin setup
    /*
     ------------------
     |..x1............. Universe
     |.......__________
     |......|x2|....... Office
     |......|.......... Institute
     |......|......x3..
     */
    var location1 = new Location(100, 100, [
        new WifiMorsel("00:19:07:06:64:00", "eduroam", -93),
        new WifiMorsel("00:19:07:06:64:01", "test", -90),
        new WifiMorsel("00:19:07:06:64:02", "welcome", -85)
    ]);
    var location2 = new Location(150, 150, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -80),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -70),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -60)
    ]);
    var location3 = new Location(200, 200, [
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -100),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -40),
        new WifiMorsel("A0:19:07:07:64:02", "welcome", -30)
    ]);

    unitTest("location_add", location1, Success, adminSession);
    unitTest("location_add", location2, Success, adminSession);
    unitTest("location_add", location3, Success, adminSession);
    // add area that covers location3
    unitTest("area_add", new Area("office", null, 145, 145, 10, 10), Success, adminSession);
    // area that covers loc2 + 3
    unitTest("area_add", new Area("institute", null, 140, 140, 100, 100), Success, adminSession);

    unitTest("area_read", new Area(), Array, adminSession);

    // end setup
    var exactOfficeLocationRequest = new Location(0, 0, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -80),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -70),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -60)
    ]);
    var closeOfficeLocationRequest = new Location(345, 212, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -81),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -69),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -62)
    ]);
    var exactInstituteLocationRequest = new Location(1, 1, [
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -100),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -40),
        new WifiMorsel("A0:19:07:07:64:02", "welcome", -30)
    ]);
    var newLocationRequest = new Location(1, 1, [
        new WifiMorsel("A0:19:07:07:64:ad", "eduroam", -88),
        new WifiMorsel("A0:19:07:07:64:ac", "eduroam", -77),
        new WifiMorsel("A0:19:07:07:64:ae", "welcome", -66)
    ]);
    var offLocationRequest = new Location(1, 1, [
        new WifiMorsel("A0:19:07:07:dd:ad", "eduroam", -88),
        new WifiMorsel("A0:19:07:07:de:ac", "eduroam", -77),
        new WifiMorsel("A0:19:07:07:df:ae", "eduroam", -66)
    ]);


    // Test exact location match to University (location1)
    // note: because of first time, area is correct on first call
    var match = unitTest("position_find", exactOfficeLocationRequest, Area, adminSession);
    unitTest("position_find", exactOfficeLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "office") {
        alert("Failed 'find_position' 1\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (office).");
    }

    // Test close location match
    unitTest("position_find", closeOfficeLocationRequest, Area, adminSession);
    match = unitTest("position_find", closeOfficeLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "office") {
        alert("Failed 'find_position' 2\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (office).");
    }

    // Test off location match
    unitTest("position_find", exactInstituteLocationRequest, Area, adminSession);
    match = unitTest("position_find", exactInstituteLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "institute") {
        alert("Failed 'find_position' 3\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (institute).");
    }

    // Test at university but no match
    unitTest("position_find", newLocationRequest, Area, adminSession);
    match = unitTest("position_find", newLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "University") {
        alert("Failed 'find_position' 4\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (University).");
    }

    // Test off location match
    unitTest("position_find", offLocationRequest, Success, adminSession);
    match = unitTest("position_find", offLocationRequest, Success, adminSession);
    if (!instanceOf(match, Success)) {
        alert("Failed 'find_position' 5\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (unknown).");
    }

    //now add more morsels and repeat the requests
    location1 = new Location(100, 100, [
        new WifiMorsel("00:19:07:06:64:00", "eduroam", -91),
        new WifiMorsel("00:19:07:06:64:01", "test", -88),
        new WifiMorsel("00:19:07:06:64:02", "welcome", -84),
        new WifiMorsel("00:19:07:06:64:00", "eduroam", -92),
        new WifiMorsel("00:19:07:06:64:01", "test", -91),
        new WifiMorsel("00:19:07:06:64:02", "welcome", -86)
    ]);
    location2 = new Location(150, 150, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -79),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -68),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -57),
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -81),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -71),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -62)
    ]);
    location3 = new Location(200, 200, [
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -100),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -40),
        new WifiMorsel("A0:19:07:07:64:02", "welcome", -30),
        new WifiMorsel("A0:19:07:07:64:00", "eduroam", -100),
        new WifiMorsel("A0:19:07:07:64:01", "eduroam", -40),
        new WifiMorsel("A0:19:07:07:64:02", "welcome", -30)
    ]);

    unitTest("location_add", location1, Success, adminSession);
    unitTest("location_add", location2, Success, adminSession);
    unitTest("location_add", location3, Success, adminSession);

    unitTest("position_find", exactOfficeLocationRequest, Area, adminSession);
    match = unitTest("position_find", exactOfficeLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "office") {
        alert("Failed 'find_position' 6\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (office).");
    }

    // Test close location match
    unitTest("position_find", closeOfficeLocationRequest, Area, adminSession);
    match = unitTest("position_find", closeOfficeLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "office") {
        alert("Failed 'find_position' 7\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (office).");
    }

    // Test otherExact location match
    unitTest("position_find", exactInstituteLocationRequest, Area, adminSession);
    match = unitTest("position_find", exactInstituteLocationRequest, Area, adminSession);
    if (!instanceOf(match, Area) || match.ID != "institute") {
        alert("Failed 'find_position' 8\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (institute).");
    }


    cleanDB();

    alert("Position done.");
}

/**
 * Tests the reading of the positions of all the users in the system, with filtering check!
 */
function testPositionRead() {
    alert("Beginning position read test.");
    cleanDB();
    var adminSession = getAdminSession();

    // Prepare testing field:
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
        new WifiMorsel("A0:19:07:07:64:02", "welcome", -30)
    ]);
    unitTest("location_add", location1, Success, adminSession);
    unitTest("location_add", location2, Success, adminSession);
    unitTest("location_add", location3, Success, adminSession);
    unitTest("area_add", new Area("office", null, 145, 145, 10, 10), Success, adminSession);
    unitTest("area_add", new Area("institute", null, 140, 140, 100, 100), Success, adminSession);
    unitTest("admin_user_add", new User("shark@ocean.int", "shark", "Haifisch Freund", false, "AVAILABLE"), Success, adminSession);
    unitTest("admin_user_add", new User("dolphin@ocean.int", "thx4fish", "Prof. Turnschwimmer", false, "AVAILABLE"), Success, adminSession);
    // Note that adminSession is on purpose; Security should ignore it in favor of login in a new user
    var sharkSession = unitTest("login", new User("shark@ocean.int", "shark"), Success, adminSession).description;
    var dolphinSession = unitTest("login", new User("dolphin@ocean.int", "thx4fish"), Success, null).description;

    // shark gets an update
    unitTest("position_find", location2, Area, sharkSession);
    // Should be only one in list here
    var userLocs = unitTest("read_all_positions", null, Array, adminSession);
    if (userLocs == null || userLocs.length != 1) {
        alert("R1: Wrong number of available user locations (not 1)!\n\n" + JSON.stringify(userLocs));
    }
    // dolphin updates
    unitTest("position_find", location3, Area, dolphinSession);
    userLocs = unitTest("read_all_positions", null, Array, adminSession);
    if (userLocs == null || userLocs.length != 2) {
        alert("R2: Wrong number of available user locations (not 2)!\n\n" + JSON.stringify(userLocs));
    }
    // test that only upon 2 consecutive new position_find the area is updated:
    var area = unitTest("position_find", location1, Area, dolphinSession);
    if (instanceOf(area, Area) && area.ID != "office") {
        alert("P1: Failed correct server side area fuzziness! Should still be office, received " + area.ID + "!\n\n" + JSON.stringify(area));
    }
    area = unitTest("position_find", location1, Area, dolphinSession);
    if (instanceOf(area, Area) && area.ID != "University") {
        alert("P2: Failed correct server side area fuzziness! Expected University, received " + area.ID + "!\n\n" + JSON.stringify(area));
    }
    // try illegal access
    //unitTest("read_all_positions", null, Error, sharkSession); TODO removed as allowed now
    // now try with public display
    unitTest("display_add", new PublicDisplay("hallway", "hallway", "Public Hallway", 34, 45), Success, adminSession);
    var pD = unitTest("login", new PublicDisplay("hallway", "hallway"), Success, null).description;
    userLocs = unitTest("read_all_positions", null, Array, pD);
    if (userLocs == null || userLocs.length != 2) {
        alert("Wrong number of available user locations for PD!\n\n" + JSON.stringify(userLocs));
    }
    // TODO test filters

    // test PD read areas
    var areas = unitTest("read_all_areas", null, Array, pD);
    if (areas == null || areas.length != 3) {
        alert("Area read failed!\n\n" + JSON.stringify(areas));
    }

    cleanDB();
    alert("Finished position read test.")
}

/**
 * Test all functionality for the public displays.
 */
function displayAdminTest() {

    alert("Beginning display admin test.");

    var adminSession = getAdminSession();
    // register some displays:
    unitTest("display_add", new PublicDisplay("office_herman", "herman_token", "Office Prof. Herman", 56, 78), Success, adminSession);
    unitTest("display_add", new PublicDisplay("instituts_sek", "sekretariat", "Sekretariat", 33, 23), Success, adminSession);
    // should fail
    unitTest("display_add", new PublicDisplay("instituts_sek", "___", "___", 343, 234), Error, adminSession);
    // key test
    unitTest("display_add", new PublicDisplay("terra", null, "Earth", 3435, 34534), Success, adminSession);
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
    unitTest("display_remove", new PublicDisplay("i don't exist!", null, null, null, null), Error, adminSession);
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

    var adminSession = getAdminSession();
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

function wifiSensorAPITest() {
    alert("Beginning WifiSensor API test!");
    cleanDB();

    var adminSession = getAdminSession();
    var sensorOne = new WifiSensor("hallway", "don't remember this, write it down!");
    var sensorTwo = new WifiSensor("my_office", null);
    // test adding new ones
    unitTest("sensor_add", sensorOne, Success, adminSession);
    var check = unitTest("sensor_add", sensorTwo, Success, adminSession);
    if (check.type != "NOTE") {
        alert("Failed to get back key!\n\n" + JSON.stringify(check));
    }
    sensorTwo.tokenHash = check.description;
    // test login for both
    var sessionOne = unitTest("login", sensorOne, Success, null).description;
    var sessionTwo = unitTest("login", sensorTwo, Success, null).description;
    // try changing pwd for two
    sensorTwo.tokenHash = "new";
    unitTest("sensor_update", sensorTwo, Success, adminSession);
    // log 2 out
    unitTest("logout", null, Success, sessionTwo);
    // try new login
    unitTest("login", sensorTwo, Success, null);
    // admin remove
    unitTest("sensor_remove", sensorTwo, Success, adminSession);
    // test
    unitTest("check", null, Error, sessionTwo);

    // todo add test for sensing capabilities
    var inOne = new SensedDevice("hallway", "192.168.178.1", "-40");
    var inTwo = new SensedDevice("hallway", "192.168.178.2", "-50");
    // illegal stuff
    unitTest("wifi_sensor_update", new User("blub", "test"), Error, sessionOne);
    unitTest("wifi_sensor_update", new SensedDevice("my_office", "255.255.255.255", "-56"), Error, sessionOne);
    // legal stuff
    unitTest("wifi_sensor_update", [inOne, inTwo], Success, sessionOne);
    // todo more

    cleanDB();
    alert("Finished WifiSensor API test.")
}

function statusTest() {
    alert("Beginning Status Test!");
    cleanDB();

    var adminSession = getAdminSession();

    //prepare test environment
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
    var wifiRequestOutside = [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -95),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -91),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -83)
    ];
    var wifiRequestInside = [
        new WifiMorsel("00:19:07:00:65:00", "eduroam", -55),
        new WifiMorsel("00:19:07:00:65:01", "welcome", -33),
        new WifiMorsel("00:19:07:00:66:02", "welcome", -11)
    ];
    unitTest("area_add", new Area("Institute", null, 10, 10, 30, 30), Success, adminSession);
    unitTest("location_add", new Location(1, 1, wifis1), Success, adminSession);
    unitTest("location_add", new Location(15, 15, wifis2), Success, adminSession);
    unitTest("display_add", new PublicDisplay("some_place", "some_token", "Some Place", 11, 11), Success, adminSession);

    var displaySession = unitTest("login", new PublicDisplay("some_place", "some_token"), Success, null).description;

    //create user
    unitTest("registration", new User("testUser@mail.de", "test", "Test Testy"), Success, null);

    //newly registered user must not be on pd
    var userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de must not be listed!\n\n" + JSON.stringify(userLocs));
    }

    // user login
    var testSession = unitTest("login", new User("testUser@mail.de", "test", null), Success, null).description;

    // logged in user that has not sent a position request is also not in the system
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de must not be listed!\n\n" + JSON.stringify(userLocs));
    }

    // make user status not null (not invisible)
    var testUser = new User("testUser@mail.de");
    testUser.status = "AVAILABLE";
    unitTest("user_update", testUser, Success, testSession);

    // logged in user that is visible but that has not sent a position request is also not in the system
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de must not be listed!\n\n" + JSON.stringify(userLocs));
    }

    // reset to null status as if first logged in without a status set
    testUser.status = null;
    unitTest("user_update", testUser, Success, testSession);

    // Start with some tracking
    // user is tracked out of institute
    var match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "University") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (University).");
    }
    // user shows now up as away == in University
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de has never set status and thus is invisible!\n\n" + JSON.stringify(userLocs));
    }

    // user is tracked inside of institute
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "Institute") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (Institute).");
    }
    // user shows now up as available == in institute
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de has never set status and thus is invisible!\n\n" + JSON.stringify(userLocs));
    }

    //user changes status to invisible which should result in equal results as null
    testUser.status = "INVISIBLE";
    unitTest("user_update", testUser, Success, testSession);

    // user is tracked out of institute
    match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "University") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (University).");
    }
    // user shows now up as away == in University
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de is invisible and must not be listed!\n\n" + JSON.stringify(userLocs));
    }

    // user is tracked inside of institute
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "Institute") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (Institute).");
    }
    // user shows now up as available == in institute
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de is invisible and must not be listed!\n\n" + JSON.stringify(userLocs));
    }

    // user changes status to visible
    testUser.status = "AVAILABLE";
    unitTest("user_update", testUser, Success, testSession);

    // From here on actual results should be listed
    // user is tracked out of institute
    match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestOutside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "University") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (University).");
    }
    // user shows now up as away == in University
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 1 || userLocs[0].position != null) {
        alert("Wrong number of available user locations (not 1)! testUser@mail.de is tracked in University, location should be null!\n\n" + JSON.stringify(userLocs));
    }

    // user is tracked inside of institute
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    match = unitTest("position_find", new Location(0, 0, wifiRequestInside), Area, testSession);
    if (!instanceOf(match, Area) || match.ID != "Institute") {
        alert("Failed 'position_find'\n\n" + JSON.stringify(match) + "\n\nPosition does not match the correct one (Institute).");
    }
    // user shows now up as available == in institute
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 1 || userLocs[0].position != "Institute") {
        alert("Wrong number of available user locations (not 1)! testUser@mail.de is tracked in Institute, location should be set!\n\n" + JSON.stringify(userLocs));
    }

    // user logs out
    unitTest("logout", null, Success, testSession);

    //logged out user must not be on pd
    userLocs = unitTest("read_all_positions", null, Array, displaySession);
    if (userLocs == null || userLocs.length != 0) {
        alert("Wrong number of available user locations (not 0)! testUser@mail.de is logged out, he must not be shown!\n\n" + JSON.stringify(userLocs));
    }

    cleanDB();
    alert("Finished Status test.")
}

/**
 * Use this method to clean the DB.
 */
function cleanDB() {
    unitTest("registration", new User("special@admin.eu", "admin"), Success, null);
    var arrival = new Arrival("login", null, new User("special@admin.eu", "admin"));
    var adminSession = JSON.parse($.ajax({
        data: JSON.stringify(arrival),
        async: false
    }).responseText).object.description;
    if (adminSession == null) {
        alert("Failed clean; could not get valid admin session!");
        return;
    }
    // Check if we are currently an admin... :P
    var admin = JSON.parse($.ajax({
        data: JSON.stringify(new Arrival("user_read", adminSession)),
        async: false
    }).responseText).object;
    if (admin.admin == "false") {
        unitTest("toggle_admin", null, Success, adminSession);
    }
    // Destroy areas
    unitTest("admin_annihilate_area", null, Success, adminSession);
    var arealist = unitTest("area_read", new Area(), Array, adminSession);
    if (arealist == null || arealist.length != 1 || arealist[0].ID != "University") {
        alert("DB was NOT CLEARED of AREAS!");
    }
    // Destroy displays
    unitTest("display_remove", new PublicDisplay(), Success, adminSession);
    var displayList = unitTest("display_read", new PublicDisplay(), Array, adminSession);
    if (displayList == null || displayList.length != 0) {
        alert("DB was NOT CLEARED of PUBLIC DISPLAYS!");
    }
    // Destroy sensors
    unitTest("sensor_remove", new WifiSensor(), Success, adminSession);
    var sensorList = unitTest("sensor_read", new WifiSensor(), Array, adminSession);
    if (sensorList == null || sensorList.length != 0) {
        alert("DB was NOT CLEARED of WIFI SENSORS!");
    }

    // Destroy users
    unitTest("ADMIN_ANNIHILATE_USER", null, Success, adminSession);
    unitTest("check", null, Error, adminSession);
    unitTest("logout", null, Success, adminSession);
}

/**
 * Small helper function that gets a valid adminSession for when that is not part of the unittest
 */
function getAdminSession() {
    var arrival = new Arrival("login", null, new User("admin@admin.admin", "admin"));
    var obj = JSON.parse($.ajax({
        data: JSON.stringify(arrival),
        async: false
    }).responseText).object;
    if (instanceOf(obj, Success)) {
        return obj.description;
    }
    alert("Failed to get admin session!");
    return null;
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
