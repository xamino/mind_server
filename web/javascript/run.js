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
function register(email, password, name) {
	var regUser = new User(email, password, name);
	// sessionHash remains empty, we don't have one yet
	var request = new Arrival("registration", null, regUser);
	send(request, function(data) {
		alert(data.object.description);
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
	send(request, function(data) {
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
	send(request, function(data) {
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
 * Checks weather the user (who wants to log in) is an admin
 */
function isAdmin(data) {
	
	var user = data.object;
	if (!(instanceOf(user,User))) {
		alert("user not user");
		logout();
	} else {
		if (data.object.admin) {
			writeCookie("MIND_Admin_C",session);
			window.location.href = "admin_home.jsp?session="+session;
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
$(document).on("submit", "#loginForm", function(event) {
	event.preventDefault();
	var email, password;
	email = $("#email").val();
	password = $("#password").val();
	//alert(email + password);

	potentialAdmin = new User(email, null, null);

	login(email, password, function() {
		doTask("user_read", potentialAdmin, isAdmin);
	});

});

/**
 * on Button click 'Register' in registration.jsp
 * reads the input data and creates a new user
 */
$(document).on("submit", "#registerForm", function(event) {
	event.preventDefault();
	var name, email, password, password2;
	name = $("#name").val();
	email = $("#email").val();
	password = $("#password").val();
	password2 = $("#password2").val();
	
	if(password != password2){
		alert("falsch");
	}
	else{
	register(email, password, name); 	//TODO: callback
	}

});

/**
 * on Button click 'Add User' in admin_user_management.jsp
 * registers a new user with the given name, email and password
 */
$(document).on("submit", "#addUserForm", function(event) {
	event.preventDefault();
	var name, email, password;
	name = $("#name").val();
	email = $("#email").val();
	password = $("#password").val();

	newUser = new User(email, password, name);

	doTask("user_add", newUser, function(event){
		var element;
		element = document.getElementById("addUserForm");
		if (element) {
		    element.innerHTML = "The user has been added. <br> <input type='button' name='ok' value='OK' onclick='window.close()'/>";
		    
		}
	});

});

/**
 * on Button click 'Edit User' in admin_user_management.jsp
 * edits the selected user
 */
$(document).on("submit", "#editUserForm", function(event) {
	event.preventDefault();
	var name, email, password;
	name = $("#name").val();
	email = $("#email").val();
	password = $("#password").val();

	//TODO: right user data
	editUser = new User(email, password, name);

	doTask("user_update", editUser, function(event){
		var element;
		element = document.getElementById("editUserForm");
		if (element) {
		    element.innerHTML = "The user has been modifyed. <br> <input type='button' name='ok' value='OK' onclick='window.close()'/>";
		    
		}
	});

});


/**
 * on Button click 'Remove User' in admin_user_management.jsp
 * removes the selected user
 */
$(document).on("submit", "#removeUserForm", function(event) {
	event.preventDefault();
	var name, email, password;
	name = $("#name").val();
	email = $("#email").val();
	password = $("#password").val();

	deleteUser = new User(email, password, name);

	doTask("user_delete", deleteUser, function(event){
		var element;
		element = document.getElementById("deleteUserForm");
		if (element) {
		    element.innerHTML = "The user has been removed. <br> <input type='button' name='ok' value='OK' onclick='window.close()'/>";
		    
		}
	});

});



/**
 * loads all users on load of page admin_user_management.jsp 
 */
function loadUsers() {
	
	users = new User(null, null, null);
	
	doTask("user_read_any", users, writeUsers);
}

function writeUsers (data){
		alert(JSON.stringify.data);
		
		//TODO if there are no users: (realize if)
//		(if ... == null){		
//			var noUserInDatabase = "There are currently no users in the database.<br> Use the button 'Add Users' to add users to the system.";
//			document.getElementById("table_space").innerHTML = noUserInDatabase;
//		}
//		else{		
	
		 var tablecontents = "";
		    tablecontents = "<table border ='1'>";
		    tablecontents += "<tr>";
		    tablecontents += "<td>User Name: </td>";
		    tablecontents += "<td>User Email: </td>";
		    tablecontents += "<td>User Password: </td>";
		    tablecontents += "<td>Edit User: </td>";
		    tablecontents += "<td>Remove User: </td>";
		    tablecontents += "</tr>";
		    
		    //TODO: user-data in table
		    for (var i = 0; i < 5; i ++)
		   {
		      tablecontents += "<tr>";
		      tablecontents += "<td>" + i + "</td>";
		      tablecontents += "<td>" + i * 1 + "</td>";
		      tablecontents += "<td>" + i * 2 + "</td>";
		      tablecontents += "<td><input type='submit' value='Edit User' onClick='javascript:popupOpen_editUser()' id='editUser" +i+ "'/></td>";
		      tablecontents += "<td><input type='submit' value='Remove User' onClick='javascript:popupOpen()' id='removeUser" +i+ "'/></td>";
		      tablecontents += "</tr>";
		   }
		   tablecontents += "</table>";
		   document.getElementById("table_space").innerHTML = tablecontents;
//		}
		
		
//	});
	


}



/**
 * Checks if the session in the url matches the user session
 * if false - return to login.jsp
 */
function checkSessionFromURL(){
	var urlSession = getURLParameter("session");
	alert("url: "+urlSession);
	var session = readCookie("MIND_Admin_C");
	alert("cookie: "+session);
	if(urlSession != session){
		alert("You have to be logged in.");
		window.location.href = "login.jsp";
		
	}
}

/**
 * Writes a Cookie - credit to http://stackoverflow.com/questions/2257631/how-create-a-session-using-javascript
 * @param name The name of the cookie (set in isAdmin)
 * @param value The value (session)
 */
function writeCookie(name,value) {
    var date, expires;
//    if (days) {
        date = new Date();
        date.setTime(date.getTime()+(15*60*1000));
        expires = "; expires=" + date.toGMTString();
//            }else{
//        expires = "";
//    }
    document.cookie = name + "=" + value + expires + "; path=/";
}

/**
 * returns a Cookie corresponding to the forwarded parameter 'name'
 * credit to http://stackoverflow.com/questions/2257631/how-create-a-session-using-javascript
 */
function readCookie(name) {
    var i, c, ca, nameEQ = name + "=";
    ca = document.cookie.split(';');
    for(i=0;i < ca.length;i++) {
        c = ca[i];
        while (c.charAt(0)==' ') {
            c = c.substring(1,c.length);
        }
        if (c.indexOf(nameEQ) == 0) {
            return c.substring(nameEQ.length,c.length);
        }
    }
    return '';
}


