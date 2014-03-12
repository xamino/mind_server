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
 * @param callback With no values!
 */
function login(email, password, callback) {
    // We can keep name empty as it is not critical to this operation
    var loginUser = new User(email, password, null);
    // sessionHash remains empty, we don't have one yet
    var request = new Arrival("login", null, loginUser);
    // send the request
    send(request, function (data) {
        // callback simply saves the session
        session = data.object.description;
        callback();
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
 * @param callback Callback(data) !
 */
function doTask(task, object, callback) {
    if (object != null) {
        send(new Arrival(task, session, object), callback);
    } else {
        send(new Arrival(task, session), callback);
    }
}

function isAdmin(data){
	
	if(!(data instanceof User)){
		alert("user not user");
	}else{
		if(data.admin){
			return true;
		}else{
			return false;
		}
	}
	
}

$(document).on("submit","#loginForm", function(event){
	event.preventDefault();
	var email, password;
	email=$("#email").val();	
	password=$("#password").val();
	alert(email + password);
	
	var potentialAdmin = new User(email, null, null);
	
	if(doTask("user_read",potentialAdmin,isAdmin(potentialAdmin))){
		var login = login(email, password);	
		var session = login.session;
		
	}else{
		alert("user is not admin");
	}
	
	
	
});

