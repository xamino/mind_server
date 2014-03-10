/**
 * ALL FUNCTIONAL CODE GOES HERE!
 */

/**
 * Sessionhash is stored here so you don't have to keep calling it.
 * @type {string}
 */
var session = "";

/**
 * Function for registering a user.
 * @param email Primary key, must be unique.
 * @param password Plaintext password (server-side hashing)
 * @param name
 */
function register(email, password, name) {
    var regUser = new User(email, password, name);
    // sessionHash remains empty, we don't have one yet
    var request = new Arrival("registration", null, regUser);
    send(request, function (data) {
        alert(data.object.description);
    });
}

/**
 * Function for login in.
 * @param email Primary key.
 * @param password Password.
 */
function login(email, password) {
    // We can keep name empty as it is not critical to this operation
    var loginUser = new User(email, password, null);
    // sessionHash remains empty, we don't have one yet
    var request = new Arrival("login", null, loginUser);
    // send the request
    send(request, function (data) {
        // callback simply saves the session
        session = data.object.description;
        alert(session);
    });
}

/**
 * Log a user out again.
 */
function logout() {
    var request = new Arrival("logout", session);
    send(request, function (data) {
        alert(data.object.description);
    });
}

/**
 * Small helpful function for testing functionality in the browser console. Mainly removes the need to keep track
 * of the sessionHash. :P
 * @param task
 * @param object
 * @param callback
 */
function doTask(task, object, callback) {
    if (object != null) {
        send(new Arrival(task, session, object), callback);
    } else {
        send(new Arrival(task, session), callback);
    }
}

/**
 * Function that runs a unit test. WARNING: test is done with synchronous ajax calls, which means the webpage will be
 * unresponsive until the unit test is done!
 */
function doUnitTest() {
    alert("Beginning unit test!");

    // Check initial registration
    // EXAMPLE FOR STRICT UNITTEST!!!
    unitTest("registration", new User("admin@admin.de", "admin", "Peter Maier"), new Success("Registered","Registered to \u0027admin@admin.de\u0027."), null, true);
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


    alert("Finished!")
}

/**
 * Runs a single test.
 * @param task The server API task to execute.
 * @param object_in The object to send.
 * @param object_out The comparison to use for the reply. Can be just a class if not using strict mode!
 * @param session If applicable, the session to attach – else just use null.
 * @param strict If set to true, the object_out will be matched fully, not just type.
 * @return The object received from the server if the unit test was successful, else null.
 */
function unitTest(task, object_in, object_out, session, strict) {
    var arrival;
    if (object_in != null) {
        arrival = new Arrival(task, session, object_in);
    }
    else {
        arrival = new Arrival(task, session);
    }
    strict = (strict == true);
    // Synchronously send:
    var response = JSON.parse($.ajax({
        data: JSON.stringify(arrival),
        async: false
    }).responseText);

    // Strict means the object must match 100%!
    if (strict) {
        if (!(response.object === object_out)) {
            alert("Failed '" + task + "'");
            return null;
        } else {
            return response.object;
        }
    } else {
        // Check if we were handed just the class or an object
        if (object_out.$type == undefined) {
            // If just a class, create object
            object_out = new object_out();
        }
        if (!(response.object.$type === object_out.$type)) {
            alert("Failed '" + task + "'");
            return null;
        } else {
            return response.object;
        }
    }
}
