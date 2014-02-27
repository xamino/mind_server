//WARNING: Changes here mean the browser must be forced to FULLY RELOAD the page, otherwise the cached version
//         is used further!

/**
 * Function for registering a user.
 * @param email Primary key, must be unique.
 * @param password Plaintext password (server-side hashing)
 * @param name
 */
function register(email, password, name) {
    var regUser = new user(email, password, name);
    // sessionHash remains empty, we don't have one yet
    var request = new arrival("REGISTRATION", null, regUser);
    send("POST", request);
}
/**
 * Function for login in.
 * @param email Primary key.
 * @param password Password.
 */
function login(email, password) {
    // We can keep name empty as it is not critical to this operation
    var loginUser = new user(email, password, null);
    // sessionHash remains empty, we don't have one yet
    var request = new arrival("LOGIN", null, loginUser);
    send("POST", request);
}

/**
 * Helper function for sending data.
 * @param type Type of request, most likely POST.
 * @param data The data to send, in JSON.
 */
function send(type, data) {
    $.ajax({
        url: '/Servlet/test',
        type: type,
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        async: false
    });
}

// OBJECT DEFINITIONS HERE –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-

function arrival(task, sessionHash, object) {
    this.$type = "Arrival";
    this.sessionHash = sessionHash;
    this.task = task;
    this.object = object;
}

function user(email, password, name) {
    this.$type = "User";
    this.email = email;
    this.pwdHash = password;
    this.name = name;
}