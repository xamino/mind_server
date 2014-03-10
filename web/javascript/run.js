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
