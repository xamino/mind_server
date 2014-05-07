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