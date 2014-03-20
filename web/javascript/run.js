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
	send(request, function(data) {
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
 * Checks weather the user (who intends to log-in) is an admin
 */
function isAdmin(data) {
	
	var user = data.object;

	if (!(instanceOf(user,User))) {
		alert("user not user");
		logout();
	} else {
		if (data.object.admin) {
			writeCookie("MIND_Admin_C_session",session);
			writeCookie("MIND_Admin_C_mail",user.email);
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
		doTask("ADMIN_USER_READ", potentialAdmin, isAdmin);
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
	register(email, password, name, null);
	}

});

////////////////old add function --> leave because of the popup example //////////////////

///**
// * on Button click 'Add User' in admin_user_management.jsp
// * registers a new user with the given name, email and password
// */
//$(document).on("submit", "#addUserForm", function(event) {
//	event.preventDefault();
//	var name, email, password;
//	name = $("#name").val();
//	email = $("#email").val();
//	password = $("#password").val();
//
//	newUser = new User(email, password, name);
//	doTask("ADMIN_USER_ADD", newUser, function(event){
//		var element;
//		element = document.getElementById("addUserForm");
//		if (element) {
//		    element.innerHTML = "The user has been added. <br> <input type='button' name='ok' value='OK' onclick='window.opener.location.reload(); window.close()'/>";
//		    
//		}
//	});
//
//});

////////////////old edit function --> leave because of the popup example //////////////////
///**
// * on Button click 'Edit User' in admin_user_management.jsp
// * edits the selected user
// */
//$(document).on("submit", "#editUserForm", function(event) {
//	event.preventDefault();
//	
////	var numberID = +currentID.replace('editUser','');
////	alert(numberID);
//	
//	//all users
//	var users = new User(null,null,null);
//	doTask("READ_ALL_ADMIN", users, function(event){
//	
//	var id = getURLParameter("id");
//		
//	alert(JSON.stringify(data));				//funzt ned --> data is iwie ned ansprechbar
////	alert(JSON.stringify(data.object[1].email));	
//		
//	var name, email, password;
//	name = $("#name").val();
//	email = $("#email").val();
//	password = $("#password").val();
//
//	//have to choose right user data
//	editUser = new User(email, password, name);
//
//	doTask("ADMIN_USER_UPDATE", editUser, function(event){
//		var element;
//		element = document.getElementById("editUserForm");
//		if (element) {
//		    element.innerHTML = "The user has been modifyed. <br> <input type='button' name='ok' value='OK' onclick='window.opener.location.reload(); window.close()'/>";
//		    
//		}
//	});	
//	});
//});



//////////////// old remove function --> leave because of the popup example //////////////////

///**
// * on Button click 'Remove User' in admin_user_management.jsp
// * removes the selected user
// */
//$(document).on("submit", "#removeUserForm", function(event) {
//	event.preventDefault();
////	var id = gup( 'id' );
////	alert("YAY - ID: "+id);
//	var users = new User(null,null,null);
//	doTask("READ_ALL_ADMIN", users, removeUser);
//});

	
///**
// * removes the selected user
// */
//function removeUser (data){
//	var id = getURLParameter("id");
////	var id = gup( 'id' );
//	alert("YAY - ID: "+id);
//		alert(JSON.stringify(data));	//session is not valid???
//		alert("da");
////		var email = data.object[1].email;
////		var password = data.object[1].password;
////		var name = data.object[1].name;
//		
//		
////		deleteUser = new User(email, password, name); 
//		}
//		
////		doTask("ADMIN_USER_DELETE", deleteUser, function(event){
////    		var element;
////    		element = document.getElementById("removeUserForm");
////    		if (element) {
////    		    element.innerHTML = "The user has been removed. <br> <input type='button' name='ok' value='OK' onclick='window.opener.location.reload(); window.close()'/>";
////    		    
////    		}
////    	});
//			
////	});
//	});
			





/**
 * loads all users on load of page admin_user_management.jsp 
 */
function loadUsers() {
	var users = new User(null,null,null);
//	var users = null;
	doTask("admin_user_read", users, writeUsers);
}

function writeUsers (data){
//		alert(JSON.stringify(data));
		if(data.object.length == null){
			var noUserInDatabase = "There are currently no users in the database.<br> Use the button 'Add Users' to add users to the system.";
			document.getElementById("table_space").innerHTML = noUserInDatabase;
		}
		else{
//		alert(JSON.stringify(data));
//		alert("Test: "+data.object.length);
	
		 var tablecontents = "";
		    tablecontents = "<table border ='1'>";
		    tablecontents += "<tr>";
		    tablecontents += "<td>User Name: </td>";
		    tablecontents += "<td>User Email: </td>";
		    tablecontents += "<td>Is Admin: </td>";
		    tablecontents += "<td>Edit User: </td>";
		    tablecontents += "<td>Remove User: </td>";
		    tablecontents += "</tr>";
		    
		    for (var i = 0; i < data.object.length; i ++)
		   {
		      tablecontents += "<tr>";
		      tablecontents += "<td>" + data.object[i].name + "</td>";
		      tablecontents += "<td>" + data.object[i].email + "</td>";
		      tablecontents += "<td>" + data.object[i].admin + "</td>";
		      //tablecontents += "<td><input type='submit' value='Edit User' onClick='javascript:popupOpen_editUser(this.id)' id='editUser" +i+ "'/></td>";
		      //tablecontents += "<td><input type='submit' value='Remove User' onClick='javascript:popupOpen_removeUser(this.id)' id='removeUser" +i+ "'/></td>";
		      tablecontents += "<td><input type='submit' value='Edit User' onClick='editUserViaPopup("+JSON.stringify(data.object[i])+")'/></td>";
		      if(data.object[i].email == readCookie("MIND_Admin_C_mail")){
		    	  tablecontents += "<td><input type='submit' value='Remove User' disabled='true' onClick='removeUserViaPopup("+JSON.stringify(data.object[i])+")'/></td>";
		      }else{
		    	  tablecontents += "<td><input type='submit' value='Remove User' onClick='removeUserViaPopup("+JSON.stringify(data.object[i])+")'/></td>";		    	  
		      }
		      tablecontents += "</tr>";												
		   }
		   tablecontents += "</table>";
		   document.getElementById("table_space").innerHTML = tablecontents;
		   
		}

}


/**
* on Button click 'Add User' in admin_user_management.jsp
* registers a new user with the given name, email and password
*/
function addUserViaPopup()
{
	
	var isadmin = confirm("Do you want the user to be an admin?");
	var userstatus = "user";
	if(isadmin){
		userstatus = "admin";
	}
	
	var name = prompt("Please enter the name of the "+userstatus+" you want to add:");
	
	if(name != null){	// if Cancel Button isn't clicked
	
		var email = prompt("Please enter the email of the "+userstatus+" you want to add:");
		
		if(email != null){	// if Cancel Button isn't clicked
		
			var password = prompt("Please enter the password of the "+userstatus+" you want to add:");
			
		//	if(password != null){	// if Cancel Button isn't clicked
		
				if (name != "" && email != "" /*&& password != ""*/){	//everything is given
					newUser = new User(email, password, name, isadmin);
					doTask("ADMIN_USER_ADD", newUser, function(data){
					if(password == "" || password==null){
						alert("The following "+userstatus+" has been added:\n"+
								"Name: "+name+"\n"+
								"Email: "+email+"\n"+
								"Generated Password: "+data.object.description);
					}else{
						alert("The following "+userstatus+" has been added:\n"+
								"Name: "+name+"\n"+
								"Email: "+email+"\n"+
								"Password: "+password);							
					}


//						var element;
//						element = document.getElementById("infoText");
//						if (element) {
//						    element.innerHTML = "The user (name: "+name+") has been added. Please click here to reload the page: <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";
//						    
//						}
						window.location.reload();		//--> text will not be visible --> button or is alert better??
					});
//				}
//				else{
//					alert("You have to specify name, email and password. None of them can be empty!");
//				}
			
			}

		}
	}
}


/**
 * on Button click 'Edit User' in admin_user_management.jsp
 * edits the selected user
 */
function editUserViaPopup(data){

	var isadmin = data.admin;
	var willbeadmin = false;
	var prevuserstatus;
	var newuserstatus;
	if(isadmin){
		prevuserstatus = "admin";
		willbeadmin = confirm("Should the admin-status of the user '"+data.name+"' be remained?");
	}else{
		prevuserstatus = "user";
		willbeadmin = confirm("Do you want to change the status of the user '"+data.name+"' to admin-status?");
	}
	
	if(willbeadmin){newuserstatus="admin";}else{newuserstatus="user";}
	
	var name = prompt("EDIT NAME - If you want to change the name: '"+data.name+"' simply enter the new name. If you don't want to change anything, leave it empty.");
	
//	var email = prompt("If you want to change the email: "+data.email+" simply enter the new email. If you don't want to change something, leave it empty.");
	
	if(name != null){	// if Cancel Button isn't clicked
	
		var password = prompt("EDIT PASSWORD - If you want a new password, simply enter the new password. If you don't want to change anything, leave it empty.");
	
		if(password != null){	// if Cancel Button isn't clicked
		
			//nothing has been changed
			if (name == "" && password == ""){
				var element;
				element = document.getElementById("infoText");
				if (element) {
				    element.innerHTML = "You didn't change anything. <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";
				    
				}
			}
			//something has been changed
			else{
				var newName = data.name, newPassword = data.password, newEmail = data.email;
				if (name != ""){
					newName = name;
				}
		//		if (email != ""){
		//			newEmail = password;
		//		}
				if (password != ""){
					newPassword = password;
				}
				
				var updateUser = new User(newEmail, newPassword, newName, willbeadmin);
				//TODO: select right user
				doTask("ADMIN_USER_UPDATE", updateUser, function(event){
//					var element;
//					element = document.getElementById("infoText");
//					if (element) {
//					    element.innerHTML = "The user (name: "+newName+") has been modified. Bis click here to reload the page: <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";
//					    
//					}
					
					alert("The following changes were made:"+
							"\nPrevious name: "+data.name+" - New name: "+newName+
							"\nPrevious email: "+data.email+" - New email: "+newEmail+
							"\nPrevious status: "+prevuserstatus+" New status: "+newuserstatus);
					window.location.reload();
				});
			}
		
		}
	}
	

}

/**
 * Creates a popup, enabling the admin to delete the user
 * @param data
 * the user data that can be deleted (JSON.stringified)
 */
function removeUserViaPopup(data)
{
	var r=confirm("Do you want to remove the user '"+data.name+"' ?");
	if (r==true)
	{
	  var usertodelete = new User(data.email,null,null);
//	  alert("FILTER OBJECT FOR USER DELETE: "+JSON.stringify(usertodelete));
	  doTask("ADMIN_USER_DELETE", usertodelete, function(event){
		  //TODO relaod page
//		var element;
//		element = document.getElementById("infoText");
//		if (element) {
//		    element.innerHTML = "The user (name:"+data.name+") has been removed. Please click here to reload the page: <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";
//		    
//		}
		  alert("The following user has been deleted:\n"+
				  "Name: "+data.name+
				  "\nEmail: "+data.email);
		window.location.reload();	//--> text will not be visible --> button or is alert better?? 
		  
  		});
	}
}



/**
 * Checks if the session in the url matches the user session
 * if false - return to login.jsp
 */
function checkSessionFromURL(){
	var urlSession = getURLParameter("session");
//	alert("url:"+urlSession);
	var session = readCookie("MIND_Admin_C_session");
//	alert("cookie:"+session);
	if(urlSession != session){
		alert("You have to be logged in.");
		window.location.href = "login.jsp";
		
	}else{
		return session;		
	}
	
	
}

/**
 * This function is called onLoad of each admin page.
 * The session will be checked by the checkSessionFromURL function
 * and the session id will be added to all links classified as "adminlink"
 */
function onLoadOfAdminPage(){
	//the current session - if correct - else this session variable is never set -> redirection to login.jsp
	session = checkSessionFromURL();
	
	//all links that are classified as "adminlink"
	var links = document.getElementsByClassName("adminlink");
	
	//add sessionid to URLs which are classified as "adminlink"
	[].forEach.call(links, function(link) {
	    //add session to link
		link.setAttribute("href",link+"?session="+session);
	});
}



