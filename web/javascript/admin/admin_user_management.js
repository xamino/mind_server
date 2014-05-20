
/****************Admin - User Management****************/


/**
 * loads all users on load of page admin_user_management.jsp
 */
function loadUsers() {
    var users = new User(null, null, null);
    doTask("admin_user_read", users, writeUsers);
}

function writeUsers(data) {
    if (data.object.length == 0) {
        var noUserInDatabase = "There are currently no users in the database.<br> Use the button 'Add Users' to add users to the system.";
        document.getElementById("table_space").innerHTML = noUserInDatabase;
    }
    else {
//		alert(JSON.stringify(data));
//		alert("Test: "+data.object.length);

        var tablecontents = "";
        tablecontents = "<table border ='1'>";
        tablecontents += "<tr>";
        tablecontents += "<td>User Name: </td>";
        tablecontents += "<td>User Email: </td>";
        tablecontents += "<td>Last Access Time: </td>";
        tablecontents += "<td>Position: </td>";
        tablecontents += "<td>Status: </td>";
        tablecontents += "<td>Is Admin: </td>";
        tablecontents += "<td>Edit User: </td>";
        tablecontents += "<td>Remove User: </td>";
        tablecontents += "</tr>";

        var lastPosition;

        for (var i = 0; i < data.object.length; i++) {
            tablecontents += "<tr>";
            tablecontents += "<td>" + data.object[i].name + "</td>";
            tablecontents += "<td>" + data.object[i].email + "</td>";
            tablecontents += "<td>" + (data.object[i].lastAccess == undefined ? "Never" : data.object[i].lastAccess) + "</td>";
            lastPosition = data.object[i].position;
            lastPosition = (lastPosition == undefined || lastPosition == null) ? "Unknown" : lastPosition;
            tablecontents += "<td>" + lastPosition + "</td>";
            tablecontents += "<td>" + data.object[i].status + "</td>";
            tablecontents += "<td>" + data.object[i].admin + "</td>";
            //tablecontents += "<td><input type='submit' value='Edit User' onClick='javascript:popupOpen_editUser(this.id)' id='editUser" +i+ "'/></td>";
            //tablecontents += "<td><input type='submit' value='Remove User' onClick='javascript:popupOpen_removeUser(this.id)' id='removeUser" +i+ "'/></td>";
            tablecontents += "<td><input type='submit' value='Edit User' onClick='editUserViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            if (data.object[i].email == readCookie("MIND_Admin_C_mail")) {
                tablecontents += "<td><input type='submit' value='Remove User' disabled='true' onClick='removeUserViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            } else {
                tablecontents += "<td><input type='submit' value='Remove User' onClick='removeUserViaPopup(" + JSON.stringify(data.object[i]) + ")'/></td>";
            }
            tablecontents += "</tr>";
        }
        tablecontents += "</table>";
        document.getElementById("table_space").innerHTML = tablecontents;

    }

}


/**
 * on Button click 'Add User' in admin_user_management.jsp
 * adds a new user with the given name, email and password
 */
function addUserViaPopup() {

    var isadmin = confirm("Do you want the user to be an admin?");
    var userstatus = "user";
    if (isadmin) {
        userstatus = "admin";
    }

    var name = prompt("Please enter the name of the " + userstatus + " you want to add:");

    if (name != null) {	// if Cancel Button isn't clicked

        var email = prompt("Please enter the email of the " + userstatus + " you want to add:");

        if (email != null) {	// if Cancel Button isn't clicked

            var password = prompt("Please enter the password of the " + userstatus + " you want to add:");

            //	if(password != null){	// if Cancel Button isn't clicked

            if (name != "" && email != "" /*&& password != ""*/) {	//everything is given
                newUser = new User(email, password, name, isadmin);
                doTask("ADMIN_USER_ADD", newUser, function (data) {
                    if (password == "" || password == null) {
                        alert("The following " + userstatus + " has been added:\n" +
                            "Name: " + name + "\n" +
                            "Email: " + email + "\n" +
                            "Generated Password: " + data.object.description);
                    } else {
                        alert("The following " + userstatus + " has been added:\n" +
                            "Name: " + name + "\n" +
                            "Email: " + email + "\n" +
                            "Password: " + password);
                    }


//						var element;
//						element = document.getElementById("infoText");
//						if (element) {
//						    element.innerHTML = "The user (name: "+name+") has been added. Please click here to reload the page: <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";
//						    
//						}
                    window.location.reload();
                });

            }

        }
    }
}


/**
 * on Button click 'Edit User' in admin_user_management.jsp
 * edits the selected user
 */
function editUserViaPopup(data) {

    var isadmin = data.admin;
    var willbeadmin = false;
    var prevuserstatus;
    var newuserstatus;
	if (isadmin == true) {
		prevuserstatus = "admin";
		if (data.email == readCookie("MIND_Admin_C_mail")) {
			willbeadmin = true;
			alert("You can't change your own status. Another admin has to change it for you.");
		}else{
			willbeadmin = confirm("Should the admin-status of the user '" + data.name + "' be remained?");
		}
	} else {
	    prevuserstatus = "user";
	    willbeadmin = confirm("Do you want to change the status of the user '" + data.name + "' to admin-status?");
	}
	if (willbeadmin) {
	    newuserstatus = "admin";
	} else {
	    newuserstatus = "user";
	}

    var name = prompt("EDIT NAME - If you want to change the name: '" + data.name + "' simply enter the new name. If you don't want to change anything, leave it empty.");

//	var email = prompt("If you want to change the email: "+data.email+" simply enter the new email. If you don't want to change something, leave it empty.");

    if (name != null) {	// if Cancel Button isn't clicked

        var password = prompt("EDIT PASSWORD - If you want a new password, simply enter the new password. If you don't want to change anything, leave it empty.");

        if (password != null) {	// if Cancel Button isn't clicked

            //nothing has been changed
            if (prevuserstatus == newuserstatus && name == "" && password == "") {
                var element;
                element = document.getElementById("infoText");
                if (element) {
                    element.innerHTML = "You didn't change anything. <input type='button' name='ok' value='OK' onclick='window.location.reload()'/>";

                }
            }
            //something has been changed
            else {
                var newName = data.name, newPassword = data.password, newEmail = data.email;
                if (name != "") {
                    newName = name;
                }
                //		if (email != ""){
                //			newEmail = password;
                //		}
                if (password != "") {
                    newPassword = password;
                }

                var updateUser = new User(newEmail, newPassword, newName, willbeadmin);
                doTask("ADMIN_USER_UPDATE", updateUser, function (event) {

                    alert("The following changes were made:" +
                        "\nPrevious name: " + data.name + " - New name: " + newName +
                        "\nPrevious email: " + data.email + " - New email: " + newEmail +
                        "\nPrevious status: " + prevuserstatus + " New status: " + newuserstatus);
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
function removeUserViaPopup(data) {
    var r = confirm("Do you want to remove the user '" + data.name + "' ?");
    if (r == true) {
        var usertodelete = new User(data.email, null, null);
//	  alert("FILTER OBJECT FOR USER DELETE: "+JSON.stringify(usertodelete));
        doTask("ADMIN_USER_DELETE", usertodelete, function (event) {
            alert("The following user has been deleted:\n" +
                "Name: " + data.name +
                "\nEmail: " + data.email);
            window.location.reload();	//--> text will not be visible --> button or is alert better??

        });
    }
}

/**
 * on Button click 'Kill all user-sessions' in admin_user_management.jsp
 * kills all sessions, also the admin-session
 */
function killUserSessions() {
	
	 var r = confirm("Do you want to remove all active user sessions (not your session)?");
	    if (r == true) {
	        doTask("KILL_SESSIONS", null, function (event) {
	            alert("All sessions have been successfully deleted.");
	            window.location.reload();

	        });
	    }
}
