/**
 * Function that runs a unit test. WARNING: test is done with synchronous ajax calls, which means the webpage will be
 * unresponsive until the unit test is done!
 */
function doUnitTest() {
    alert("Beginning unit test!");

    // Check initial registration
    unitTest("registration", new User("admin@admin.de", "admin", "Peter Maier"), Success, null);
    // try registering again
    unitTest("registration", new User("admin@admin.de", "admin", "Peter Maier"), Error, null);
    // try login
    var adminSession = unitTest("login", new User("admin@admin.de", "admin", null), Success, null).description;
    // try logout
    unitTest("logout", null, Success, adminSession);

    alert("Registration, login, logout done.");

    // Relogin the admin
    adminSession = unitTest("login", new User("admin@admin.de", "admin", null), Success, null).description;
    // try admin access
    unitTest("admin_read_all", null, Error, adminSession);
    // Switch to admin rights:
    unitTest("toggle_admin", null, Success, adminSession);
    // Do an admin task:
    unitTest("admin_read_all", null, Array, adminSession);

    alert("Admin done.")

    // Clean remaining with admin:
    unitTest("user_delete", null, Success, adminSession);

    alert("Cleaning done. Finished!")
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
