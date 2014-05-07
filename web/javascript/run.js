/**
 * ALL FUNCTIONAL CODE GOES HERE!
 */

/**
 * Sessionhash is stored here so you don't have to keep calling it.
 *
 * @type {string}
 */
var session = "";

/**
 * Function for registering a user.
 *
 * @param email
 *            Primary key, must be unique.
 * @param password
 *            Plaintext password (server-side hashing)
 * @param name
 */
function register(email, password, name, callback) {		//needed callback here (from Laura)
    var regUser = new User(email, password, name);
    // sessionHash remains empty, we don't have one yet
    var request = new Arrival("registration", null, regUser);
    send(request, function (data) {
        alert(data.object.description);
        callback();
    });
}

/**
 * Function for login in.
 *
 * @param email
 *            Primary key.
 * @param password
 *            Password.
 * @param callback
 *            With no values!
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
 * Function for login in - public display.
 *
 * @param identification
 *            Primary key.
 * @param password
 *            Password.
 * @param callback
 *            With no values!
 */
function loginDisplay(identification, password, callback) {
    // We can keep name empty as it is not critical to this operation
    var loginDisplay = new PublicDisplay(identification, password, null, 0, 0);
    // sessionHash remains empty, we don't have one yet
    var request = new Arrival("login", null, loginDisplay);
    // send the request
    send(request, function (data) {
        // callback simply saves the session
        session = data.object.description;
        writeCookie("MIND_PD_C", session);
        //alert("PD_session: " + session);
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
 * Small helpful function for testing functionality in the browser console.
 * Mainly removes the need to keep track of the sessionHash. :P
 *
 * @param task
 * @param object
 * @param callback
 *            Callback(data) !
 */
function doTask(task, object, callback) {
    if (object != null) {
        send(new Arrival(task, session, object), callback);
    } else {
        send(new Arrival(task, session), callback);
    }
}

/**
 * Logout on button click
 */
//$(document).on("submit", "#logout", function (event) {
function userLogout(){
	doTask("LOGOUT", null, function (event) {
        alert("Your have been successfully logged out.");
        window.location='index.jsp';
    });
}

/**
 * Checks weather the user (who intends to log-in) is an admin
 */
function isAdmin(data) {

    var user = data.object[0];
    if (!(instanceOf(user, User))) {
//        alert("user not user");
        logout();
    } else {
        if (user.admin) {
            writeCookie("MIND_Admin_C_session", session);
            writeCookie("MIND_Admin_C_mail", user.email);
            window.location.href = "admin_home.jsp";
        } else {
            //TODO write cookie for user
            alert("user not admin");
            logout();
        }
    }

}

var potentialAdmin;

/**
 * on Button click 'Login' in login.jsp
 * reads the input data and checks weather this user is an admin
 */
$(document).on("submit", "#loginForm", function (event) {
    event.preventDefault();
    var email, password;
    email = $("#email").val();
    password = $("#password").val();
    //alert(email + password);

    potentialAdmin = new User(email, null, null);

    login(email, password, function () {
        doTask("ADMIN_USER_READ", potentialAdmin, isAdmin);
    });

});

/**
 * on Button click 'Register' in registration.jsp
 * reads the input data and creates a new user
 */
$(document).on("submit", "#registerForm", function (event) {
    event.preventDefault();
    var name, email, password, password2;
    name = $("#name").val();
    email = $("#email").val();
    password = $("#password").val();
    password2 = $("#password2").val();

    if (password != password2) {
        alert("falsch");
    }
    else {
        register(email, password, name, null);
    }

});


/**
 * on Button click 'Login' in public_display_login.jsp
 * reads the input data
 */
$(document).on("submit", "#loginDisplayForm", function (event) {
    event.preventDefault();
    var identification, password;
    identification = $("#identification").val();
    password = $("#password").val();

    loginDisplay(identification, password, function (event) {
        window.location.href = "public_display_start.jsp";
    });

//		doTask("ADMIN_USER_READ", potentialAdmin, isAdmin);


});




function mute() {
    alert("TODO: mute");
}

function logoutDisplay() {
    logout();
    deleteCookie("MIND_PD_C");
    window.location = "public_display_login.jsp";
}

/******************** session/cookie*****************/

/**
 * This function is called onLoad of each admin page.
 * The session will be checked by the checkSessionFromURL function
 * and the session id will be added to all links classified as "adminlink"
 */
function onLoadOfAdminPage() {
    session = readCookie("MIND_Admin_C_session");
    send(new Arrival("check",session),function(data){
    	if (instanceOf(data.object,Error)) {
    		alert("You have to be logged in.");
    		window.location.href = "login.jsp";
    	} else {
    		return session;
    	}
    });
}

/**
 * This function is called onLoad of each PD page.
 */
function onLoadOfPdPage() {
    session = readCookie("MIND_PD_C");
    send(new Arrival("check",session),function(data){
    	if (instanceOf(data.object,Error)) {
    		alert("You have to be logged in.");
    		window.location.href = "public_display_login.jsp";
    		
    	}else{
    		initPublicDisplayStuff();    		
    	}
    });
}

/**
 * This function is called when the public display attempts to display
 * the locations of the users (their icons) on the map
 */
function displayUserLocations(){

	//send(new Arrival("read_all_positions", session), retriveOriginalMetrics);
	handleAllUsersPositionData();
}

function handleAllUsersPositionData(){
	
//TESTAREA
	//User(email, password, name, admin) {
//	var user1 = new User("a@a.a",null,"a",false);
//	user1.lastPosition = 3304;
//	user1.status = "AVAILABLE";
////	user1.iconRef = "crab.png";
//	var user2 = new User("b@b.b",null,"b",false);
//	user2.lastPosition = 3304;
//	user2.status = "OCCUPIED";
////	user2.iconRef = "lion.png";
//	
//	var users = new Array();
//	users[0] = user1;
//	users[1] = user2;
//	initPublicDisplayStuff(users);
//END TESTAREA
	initPublicDisplayStuff();
}