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

    cleanDB();

    alert("Comprehensive unit test finished!");
}

function adminRightsTest() {
    alert("Beginning admin rights test.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    // Deactivate admin
    unitTest("toggle_admin", null, Success, adminSession);

    // try admin access
    unitTest("admin_read_all", null, Error, adminSession);
    // Switch to admin rights:
    unitTest("toggle_admin", null, Success, adminSession);
    // Do an admin task:
    unitTest("admin_read_all", null, Array, adminSession);

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
    // illegal login
    unitTest("login", new User("mister.x@world.org", "bomb", null), Error, null);
    // try logout
    unitTest("logout", null, Success, adminSession);
    // illegal logout
    unitTest("logout", null, Success, "not_a_hash");
    // Re-login the admin
    adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;

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
    alert("Beginning area, location, position.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;

    var wifis1 = [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -93),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -90),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -85)
    ];
    unitTest("area_add", new Area("Office Prof. Gott", [new Location(34, 57, wifis1)], 34, 56, 3, 4), Success, adminSession);
    // test universe area
    var wifis2 = [
        new WifiMorsel("00:19:07:00:65:00", "eduroam", -54),
        new WifiMorsel("00:19:07:00:65:01", "welcome", -34),
        new WifiMorsel("00:19:07:00:66:02", "welcome", -12)
    ];
    unitTest("location_add", new Location(1, 1, wifis2), Success, adminSession);
    var uniArea = unitTest("area_read", new Area("universe", null, 0, 0, 0, 0), Area, adminSession);
    if (!(uniArea instanceof Area && uniArea.locations.length >= 1)) {
        alert("Writing a location directly to the universal area failed!");
    }

    cleanDB();

    alert("Area, Location, Position done.");
}

function positionTest() {
    alert("Beginning position.");

    var adminSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;

    var location1 = new Location(100, 100, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -93),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -90),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -85)
    ]);

    var location2 = new Location(200, 200, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -80),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -70),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -60)
    ]);

    var locationRequest = new Location(0, 0, [
        new WifiMorsel("00:19:07:07:64:00", "eduroam", -92),
        new WifiMorsel("00:19:07:07:64:01", "eduroam", -91),
        new WifiMorsel("00:19:07:07:64:02", "welcome", -84)
    ]);

    unitTest("location_add", location1, Success, adminSession);
    unitTest("location_add", location2, Success, adminSession);
    var foundPosition = unitTest("position_find", locationRequest, Location, adminSession);

    // Becuase we night not find the position :P
    if (foundPosition != null) {
        if (locationRequest.coordinateX != foundPosition.coordinateX || locationRequest.coordinateY != foundPosition.coordinateY) {
            alert("Failed '" + find_position + "'\n\n" + JSON.stringify(foundPosition) + "\n\nPosition does not match the correct one.");
        }
    }

    cleanDB();

    alert("Position done.");
}

/**
 * Use this method to clean the DB.
 */
function cleanDB() {
    unitTest("registration", new User("special@admin.eu", "admin", ""), Success, null);
    var adminSession = unitTest("login", new User("special@admin.eu", "admin", null), Success, null).description;
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
        alert("DB was NOT CLEARED of AREA!");
    }

    // Destroy users
    unitTest("ADMIN_ANNIHILATE_USER", null, Success, adminSession);
    // Now check with default admin, as admin above was now deleted!
    var newSession = unitTest("login", new User("admin@admin.admin", "admin", null), Success, null).description;
    var list = unitTest("admin_read_all", null, Array, newSession);
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
        alert("Failed '" + task + "'\n\n" + JSON.stringify(response));
        return null;
    } else {
        return response;
    }
}
